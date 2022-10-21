package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.model.enumerados.EstadoSolicitud;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.ProductoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

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

    public void bloquearUsuario(UUID idUsuario, String motivo) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        usuario.setEstado(EstadoUsuario.Bloqueado);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo(usuario.getCorreo(), "Usted ha sido bloqueado del sitio ShopNow por el siguiente motivo:\n" + motivo + ".\n Para más información comunicarse con el soporte de la aplicación.", "Usuario bloqueado en ShopNow");
    }

    public void desbloquearUsuario(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Bloqueado).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        usuario.setEstado(EstadoUsuario.Activo);
        usuarioRepository.save(usuario);
        googleSMTP.enviarCorreo(usuario.getCorreo(), "Usted ha sido desbloqueado del sitio ShopNow, ya puede volver a utilizar su cuenta con normalidad al recibir este mensaje.", "Usuario desbloqueado en ShopNow");
    }

    public void eliminarUsuario(UUID idUsuario) {
        Usuario usuario = usuarioRepository.findById(idUsuario).orElseThrow(() -> new Excepcion("El usuario no existe"));
        if (usuario.getEstado() == EstadoUsuario.Eliminado) {
            throw new Excepcion("El usuario ya se encuentra en ese estado");
        }
        if (usuario instanceof Generico) {
            for (Compra compra : ((Generico) usuario).getCompras().values()) {
                if (compra.getEstado() != EstadoCompra.Completada || compra.getEstado() != EstadoCompra.Cancelada) {
                    //Devolucion
                    break;
                }
            }
            for (Compra venta : ((Generico) usuario).getVentas().values()) {
                if (venta.getEstado() != EstadoCompra.Completada || venta.getEstado() != EstadoCompra.Cancelada) {
                    //Devolucion
                    break;
                }
            }
        }
        usuario.setEstado(EstadoUsuario.Eliminado);
        usuarioRepository.save(usuario);
    }

    public void revisarSolicitudNuevoVendedor(UUID idUsuario, boolean aceptar, String motivo) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Bloqueado).orElseThrow(() -> new Excepcion("El usuario no existe o no se encuentra en un estado valido"));
        if (usuario instanceof Administrador) {
            throw new Excepcion("El usuario no esta disponible para esta funcionalidad");
        }
        if (((Generico) usuario).getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
            throw new Excepcion("El usuario no disponible para esta funcionalidad");
        }

        if (aceptar) {
            ((Generico) usuario).getDatosVendedor().setEstadoSolicitud(EstadoSolicitud.Aceptado);
            usuarioRepository.save(usuario);
            googleSMTP.enviarCorreo(usuario.getCorreo(), "Su solicitud para convertirse en vendedor ah sido aceptada con éxito, vuelva a iniciar sesión para obtener sus nuevas funcionalidades.\nAdemás su producto enviado en la solicitd se ah coloca a la venta", "Solicitud de vendedor aceptada");
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
            googleSMTP.enviarCorreo(usuario.getCorreo(), "Su solicitud para convertirse en vendedor ah sido rechazada, motivo:\n" + motivo + "\n Para mas información comunicarse con el soporte de la página", "Solicitud de vendedor rechazada");
        }
    }

    public void crearAdministrador(DtUsuario datos) throws NoSuchAlgorithmException {
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
