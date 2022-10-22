package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtUsuarioSlim;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
public class AdministradorService {

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ProductoRepository productoRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    DatosVendedorRepository datosVendedorRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    BraintreeUtils braintreeUtils;

    @Autowired
    CompraRepository compraRepository;

    public void bloquearUsuario(UUID idUsuario, String motivo) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        usuario.setEstado(EstadoUsuario.Bloqueado);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo(usuario.getCorreo(), "Usted ha sido bloqueado del sitio ShopNow por el siguiente motivo:\n" + motivo + ".\nPara más información comunicarse con el soporte de la aplicación.", "Usuario bloqueado - ShopNow");
    }

    public void desbloquearUsuario(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Bloqueado).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        usuario.setEstado(EstadoUsuario.Activo);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo(usuario.getCorreo(), "Usted ha sido desbloqueado del sitio ShopNow, ya puede volver a utilizar su cuenta con normalidad al recibir este mensaje.", "Usuario desbloqueado - ShopNow");
    }

    public void eliminarUsuario(UUID idUsuario, String motivo) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new Excepcion("El usuario no existe"));
        if (usuario.getEstado() == EstadoUsuario.Eliminado) {
            throw new Excepcion("El usuario ya se encuentra en ese estado");
        }
        if (usuario instanceof Generico) {
            for (Compra compra : ((Generico) usuario).getCompras().values()) {
                if (compra.getEstado() == EstadoCompra.EsperandoConfirmacion) {
                    braintreeUtils.devolverDinero(compra.getIdTransaccion());
                    Generico vendedor = compraRepository.obtenerVendedor(compra.getId());
                    googleSMTP.enviarCorreo(vendedor.getCorreo(), "La venta que usted realizó a " + usuario.getNombre() + " " + usuario.getApellido() + " (con el identificador " + compra.getId() + ") fue cancelada debido a que la cuenta del comprador ha sido eliminada del sistema.\nCualquier inconveniente comunicarse con el soporte.", "Venta cancelada - ShopNow");
                }
            }
            for (Compra venta : ((Generico) usuario).getVentas().values()) {
                if (venta.getEstado() == EstadoCompra.EsperandoConfirmacion) {
                    braintreeUtils.devolverDinero(venta.getIdTransaccion());
                    String nombreParaMostrar = (((Generico) usuario).getDatosVendedor().getNombreEmpresa() != null) ? ((Generico) usuario).getDatosVendedor().getNombreEmpresa() : usuario.getNombre() + " " + usuario.getApellido();
                    Generico comprador = compraRepository.obtenerComprador(venta.getId());
                    googleSMTP.enviarCorreo(comprador.getCorreo(), "La compra que usted realizó a " + nombreParaMostrar + " fue cancelada debido a que la cuenta del vendedor ha sido eliminada del sistema. Se ha devuelto el dinero de la compra (" + venta.getId() + ") a la tarjeta con la cual realizó el pago.\nCualquier inconveniente comunicarse con el soporte.", "Compra cancelada- ShopNow");
                }
            }
            for (Producto producto : ((Generico) usuario).getProductos().values()) {
                producto.setEstado(EstadoProducto.BloqueadoADM);
            }
        }
        usuario.setEstado(EstadoUsuario.Eliminado);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo(usuario.getCorreo(), "Su cuenta ha sido eliminada del sitio ShopNow por el siguiente motivo:\n" + motivo + ".\nPara más información comunicarse con el soporte de la aplicación.", "Usuario eliminado - ShopNow");
    }

    public void respuestaSolicitud(UUID idUsuario, boolean aceptar, String motivo) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        if (usuario instanceof Administrador) {
            throw new Excepcion("El usuario no esta disponible para esta funcionalidad");
        }
        if (((Generico) usuario).getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
            throw new Excepcion("Usuario seleccionado no disponible para esta funcionalidad");
        }

        if (aceptar) {
            ((Generico) usuario).getDatosVendedor().setEstadoSolicitud(EstadoSolicitud.Aceptado);
            usuarioRepository.save(usuario);
            googleSMTP.enviarCorreo(usuario.getCorreo(), "Su solicitud para convertirse en vendedor ah sido aceptada con éxito, vuelva a iniciar sesión para obtener sus nuevas funcionalidades.\nAdemás su producto enviado en la solicitud se colocó a la venta", "Solicitud de vendedor aceptada");
            for (Producto producto : ((Generico) usuario).getProductos().values()) { //Solo seria uno.
                producto.setEstado(EstadoProducto.Activo);
                productoRepository.save(producto);
            }
        } else {
            Generico solicitante = (Generico) usuario;
            Optional<UUID> primeraKey = solicitante.getProductos().keySet().stream().findFirst();
            UUID idProducto;
            if (primeraKey.isPresent())
                idProducto = primeraKey.get();
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en el sistema");
            int datosVendedorViejo = solicitante.getDatosVendedor().getId();
            solicitante.getProductos().clear(); //Vacio el map
            solicitante.setDatosVendedor(null); //Vacio solicitud
            usuarioRepository.saveAndFlush(solicitante);
            Optional<Producto> res = productoRepository.findById(idProducto);
            Producto producto;
            if (res.isPresent())
                producto = res.get();
            else
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error en el sistema");
            //Borrar imagenes de Storage?
            producto.getImagenesURL().clear();
            productoRepository.saveAndFlush(producto);
            productoRepository.eliminarProductoCategoria(idProducto);
            productoRepository.delete(producto);
            datosVendedorRepository.deleteById(datosVendedorViejo);
            googleSMTP.enviarCorreo(usuario.getCorreo(), "Su solicitud para convertirse en vendedor ah sido rechazada, motivo:\n\n" + motivo + "\n\nPara mas información comunicarse con el soporte de la página.", "Solicitud de vendedor rechazada");
        }
    }

    public void crearAdministrador(DtUsuarioSlim datos) throws NoSuchAlgorithmException {
        if (usuarioRepository.existsByCorreoAndEstado(datos.getCorreo(), EstadoUsuario.Activo))
            throw new Excepcion("El correo ya existe");
        String chrs = "0123456789abcdefghijklmnopqrstuvwxyz-_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        String contrasena = secureRandom.ints(16, 0, chrs.length()).mapToObj(chrs::charAt)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append).toString();
        Administrador administrador = Administrador.builder()
                .correo(datos.getCorreo())
                .nombre(datos.getNombre())
                .apellido(datos.getApellido())
                .password(passwordEncoder.encode(contrasena))
                .build();
        usuarioRepository.save(administrador);
        googleSMTP.enviarCorreo(administrador.getCorreo(), "Se ha creado su cuenta de administrador con los siguientes datos de inicio de sesión\n\nCorreo: " + datos.getCorreo() + "\nContraseña: " + contrasena + "", "Nueva cuenta de adminstrador - ShopNow");
    }
}
