package com.shopnow.shopnow.service;


import com.shopnow.shopnow.controller.responsetypes.CreditCardRef;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.repository.DatosVendedorRepository;
import com.shopnow.shopnow.repository.TarjetasRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;


@Service
public class UsuarioService {

    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    DatosVendedorRepository datosVendedorRepository;
    @Autowired
    TarjetasRepository tarjetasRepository;
    @Autowired
    FirebaseStorageService firebaseStorageService;
    @Autowired
    BraintreeUtils braintreeUtils;
    @Autowired
    GoogleSMTP googleSMTP;


    public DtUsuario infoUsuario(String uuid) {

        Optional<Usuario> usuarioBaseDatos = usuarioRepository.findByIdAndEstado(UUID.fromString(uuid), EstadoUsuario.Activo);

        if (usuarioBaseDatos.isPresent() && usuarioBaseDatos.get() instanceof Administrador adm) {
            return DtUsuario.builder()
                    .nombre(adm.getNombre())
                    .rol(Rol.ADM)
                    .build();
        }

        Generico usuario = (Generico) usuarioBaseDatos.orElseThrow(() -> new Excepcion("El usuario no existe"));

        DtDatosVendedor datosvendedor = null;

        /*      Obtencion de calificaciones  comprador   */
        Map<UUID, Calificacion> calificaciones = usuario.getCalificaciones();
        float sumaCalificacionComprador = 0, calificacionComprador = 0;
        if (calificaciones.size() != 0) {
            for (Calificacion calificacion : calificaciones.values()) {
                sumaCalificacionComprador += calificacion.getPuntuacion();
            }
            calificacionComprador = sumaCalificacionComprador / calificaciones.size();
        }

        /*     Informacion de la parte vendedor    */
        if (usuario.getDatosVendedor() != null && (usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado || usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Pendiente)) {
            Map<UUID, Compra> ventas = usuario.getVentas();
            float sumaCalificacionVendedor = 0, calificacionVendedor = 0;
            if (ventas.size() != 0) {
                int ventasCalificacion = 0;
                for (Compra venta : ventas.values()) {
                    if (venta.getInfoEntrega().getCalificaciones().isEmpty()) {
                        continue;
                    }
                    for (Calificacion calificacionItem : venta.getInfoEntrega().getCalificaciones()) {
                        if (calificacionItem.getAutor().getId().compareTo(usuario.getId()) != 0) {
                            sumaCalificacionVendedor += calificacionItem.getPuntuacion();
                            ventasCalificacion++;
                        }
                    }
                }
                if (ventasCalificacion == 0)
                    calificacionVendedor = 0;
                else
                    calificacionVendedor = sumaCalificacionVendedor / ventasCalificacion;
            }
            datosvendedor = DtDatosVendedor.builder().
                    rut(usuario.getDatosVendedor().getRut())
                    .nombreEmpresa(usuario.getDatosVendedor().getNombreEmpresa())
                    .telefonoEmpresa(usuario.getDatosVendedor().getTelefonoEmpresa())
                    .locales(usuario.getDatosVendedor().getLocales())
                    .estadoSolicitud(usuario.getDatosVendedor().getEstadoSolicitud()).calificacion(calificacionVendedor).build();
        }


        return DtUsuario.builder()
                .nombre(usuario.getNombre())
                .apellido(usuario.getApellido())
                .correo(usuario.getCorreo())
                .imagen(DtImagen.builder().data(usuario.getImagen()).build())
                .telefono(usuario.getTelefono())
                .calificacion(calificacionComprador)
                .datosVendedor(datosvendedor).build();
    }


    public void modificarInfoBasica(UUID uuid, DtUsuario usuario) {

        Generico usuarioBD;
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(uuid, EstadoUsuario.Activo);
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuarioBD = (Generico) res.get();
        }
        if (usuario.getCorreo() != null && !usuario.getCorreo().equals(usuarioBD.getCorreo())
                && (usuarioRepository.existsByCorreoAndEstado(usuario.getCorreo(), EstadoUsuario.Activo)
                || usuarioRepository.existsByCorreoAndEstado(usuario.getCorreo(), EstadoUsuario.Bloqueado)))
            throw new Excepcion("El correo ya esta en uso");

        usuarioBD.setCorreo(usuario.getCorreo());
        usuarioBD.setNombre(usuario.getNombre());
        usuarioBD.setApellido(usuario.getApellido());
        usuarioBD.setTelefono(usuario.getTelefono());

        usuarioRepository.save(usuarioBD);

    }

    public void modificarImagen(UUID idUsuario, MultipartFile imagen) throws IOException {
        if (imagen.isEmpty()) {
            throw new Excepcion("No se envio ninguna imagen");
        }
        Usuario usuario = usuarioRepository.findByIdAndEstado(idUsuario, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("El usuario no esta disponible para esta funcionalidad."));
        String newUrl = firebaseStorageService.uploadFile(imagen, idUsuario + "--imgPerfil");
        usuario.setImagen(newUrl);
        usuarioRepository.save(usuario);
    }

    public boolean esvendedor(UUID id) {
        Generico usuario;
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
            return usuario.getDatosVendedor() != null && usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado;
        }
    }


    public void modificarDatosUsuario(UUID id, DtModificarUsuario datos) {
        Generico usuario;
        Optional<Usuario> res = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        if (res.isEmpty()) {
            throw new Excepcion("El usuario no existe");
        } else {
            usuario = (Generico) res.get();
        }
        if (datos.getCorreo() != null) {
            if (usuarioRepository.existsByCorreoAndEstado(datos.getCorreo(), EstadoUsuario.Activo) || usuarioRepository.existsByCorreoAndEstado(datos.getCorreo(), EstadoUsuario.Bloqueado)) {
                throw new Excepcion("El correo nuevo ya existe.");
            } else
                usuario.setCorreo(datos.getCorreo());
        }

        if (datos.getContrasenaVieja() != null && datos.getContrasenaNueva() != null) {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            if (encoder.matches(datos.getContrasenaVieja(), usuario.getPassword())) {
                String nuevaContrasena = encoder.encode(datos.getContrasenaNueva());
                usuario.setPassword(nuevaContrasena);
            } else {
                throw new Excepcion("Contrase??a actual incorrecta. No se realiz?? la modificaci??n.");
            }
        }
        if (datos.getTelefonoContacto() != null)
            usuario.setTelefono(datos.getTelefonoContacto());

        if (usuario.getDatosVendedor() != null && usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
            //Esta parte de la funcionalidad deberia estar solo para vendedores aceptados, igualmente valido

            /* TODO Validacion del telefono :)
            if (datos.getTelefonoEmpresa().length() >= 8 || datos.getTelefonoEmpresa().matches("[+-]?\\d*(\\.\\d+)?")) {
                throw new Excepcion("El numero de telefono es invalido");
            }
        */
            if (datos.getNombreEmpresa() != null && datosVendedorRepository.existsByNombreEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El nombre de la empresa ya existe.");
            else
                usuario.getDatosVendedor().setNombreEmpresa(datos.getNombreEmpresa());

            if (datos.getTelefonoEmpresa() != null && datosVendedorRepository.existsByTelefonoEmpresa(datos.getNombreEmpresa()))
                throw new Excepcion("El numero de telefono de la empresa ya esta en uso.");
            else
                usuario.getDatosVendedor().setTelefonoEmpresa(datos.getTelefonoEmpresa());
        }

        usuarioRepository.save(usuario);
    }

    //TODO Verificar tarjeta duplicada para el usuario y verificar tarjeta existente en el sistema en general
    public void agregarTarjeta(DtTarjeta dtTarjeta, UUID userId) {
        Optional<Usuario> user = usuarioRepository.findByIdAndEstado(userId, EstadoUsuario.Activo);
        if (user.isPresent() && user.get() instanceof Generico usuarioGenerico) {
            if (usuarioGenerico.getBraintreeCustomerId() == null) {
                String braintreeId = braintreeUtils.generateCustomerId(usuarioGenerico);
                if (braintreeId == null) throw new Excepcion("Ha ocurrido un error inesperado");
                usuarioGenerico.setBraintreeCustomerId(braintreeId);
                usuarioRepository.save(usuarioGenerico);
            }
            Tarjeta nuevaTarjeta = braintreeUtils.agregarTarjeta(dtTarjeta, usuarioGenerico.getBraintreeCustomerId());
            if (nuevaTarjeta == null) throw new Excepcion("Ha ocurrido un error al agregar la tarjeta");
            tarjetasRepository.save(nuevaTarjeta);
            usuarioGenerico.getTarjetas().put(nuevaTarjeta.getIdTarjeta(), nuevaTarjeta);
            usuarioRepository.save(usuarioGenerico);
        } else {
            throw new Excepcion("Ha ocurrido un error inesperado");
        }
    }

    public List<CreditCardRef> getTarjetas(UUID id) {
        Optional<Usuario> user = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo);
        ArrayList<CreditCardRef> list = new ArrayList<>();
        if (user.isPresent() && user.get() instanceof Generico usuarioGenerico) {
            usuarioGenerico.getTarjetas().values().forEach(tarjeta -> {
                CreditCardRef ref = new CreditCardRef(
                        tarjeta.getIdTarjeta(),
                        tarjeta.getLast4(),
                        tarjeta.getImageUrl(),
                        tarjeta.getVencimiento());
                list.add(ref);
            });
        }
        return list;
    }

    public Map<String, Object> listadoDeUsuarios(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltrosUsuario filtros) {

        if (!sortBy.matches("nombre|apellido|correo")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Usuario> usuarios;

        List<UUID> usuariosCumplenFiltro = new ArrayList<>();
        boolean isEmpty = false;

        if (filtros != null) {
            List<UUID> usuariosIdConNombre = null;
            if (filtros.getNombre() != null && !filtros.getNombre().equals("")) {
                usuariosIdConNombre = usuarioRepository.usuarioNombreApellido(filtros.getNombre().toLowerCase());
                if (usuariosIdConNombre.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConApellido = null;
            if (filtros.getApellido() != null && !filtros.getApellido().equals("")) {
                usuariosIdConApellido = usuarioRepository.usuariosConApellido(filtros.getApellido());
                if (usuariosIdConApellido.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConCorreo = null;
            if (filtros.getCorreo() != null && !isEmpty && !filtros.getCorreo().equals("")) {
                usuariosIdConCorreo = usuarioRepository.usuariosConCorreo(filtros.getCorreo());
                if (usuariosIdConCorreo.isEmpty()) isEmpty = true;
            }
            List<UUID> usuariosIdConEstado = null;
            if (filtros.getEstado() != null && !isEmpty) {
                usuariosIdConEstado = usuarioRepository.usuariosConEstado(filtros.getEstado().toString());
                if (usuariosIdConEstado.isEmpty()) isEmpty = true;
            }
            if (!isEmpty)
                usuariosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), usuariosIdConNombre, usuariosIdConApellido, usuariosIdConCorreo, usuariosIdConEstado).stream().toList();
            usuarios = usuarioRepository.findByIdIn(usuariosCumplenFiltro, pageable);
        } else
            usuarios = usuarioRepository.todosLosUsuarios(pageable);

        List<Usuario> listaDeUsuarios = usuarios.getContent();

        List<DtUsuarioSlim> content = listaDeUsuarios.stream().map(this::getDtUsuarioSlim).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("usuarios", content);
        response.put("currentPage", usuarios.getNumber());
        response.put("totalItems", usuarios.getTotalElements());
        response.put("totalPages", usuarios.getTotalPages());

        return response;
    }

    public void eliminarMiCuenta(UUID id) {
        Usuario usuario = usuarioRepository.findByIdAndEstado(id, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("Usuario no disponible para esta funcionalidad"));
        if (usuario instanceof Administrador) {
            throw new Excepcion("Tipo de usuario equivocado");
        }
        Generico usuarioEliminar = (Generico) usuario;
        if (usuarioEliminar.getEstado() == EstadoUsuario.Eliminado) {
            throw new Excepcion("El usuario ya se encuentra en ese estado");
        }

        boolean productosActivos = false;
        boolean comprasActivas = false;
        boolean ventasActivas = false;
        for (Compra compras : usuarioEliminar.getCompras().values()) {
            if (compras.getEstado() != EstadoCompra.Completada || compras.getEstado() != EstadoCompra.Cancelada) {
                comprasActivas = true;
                break;
            }
        }
        for (Compra ventas : usuarioEliminar.getVentas().values()) {
            if (ventas.getEstado() != EstadoCompra.Completada || ventas.getEstado() != EstadoCompra.Cancelada) {
                ventasActivas = true;
                break;
            }
        }
        for (Producto producto : usuarioEliminar.getProductos().values()) {
            if (producto.getEstado() == EstadoProducto.Activo) {
                productosActivos = true;
                break;
            }
        }
        if (productosActivos || comprasActivas || ventasActivas) {
            throw new Excepcion("No se puede realizar la eliminacion de la cuenta debido a que tiene: " +
                    ((productosActivos) ? "Productos activos. " : "") +
                    ((comprasActivas) ? "Compras en proceso. " : "") +
                    ((ventasActivas) ? "Ventas en proceso." : ""));
        }
        usuarioEliminar.setEstado(EstadoUsuario.Eliminado);
        usuarioRepository.save(usuarioEliminar);
        googleSMTP.enviarCorreo(usuarioEliminar.getCorreo(), "Su cuenta en ShopNow (correo: " + usuario.getCorreo() + ") ha sido eliminada satisfactoriamente.", "Cuenta eliminada - ShopNow");
    }

    private DtUsuarioSlim getDtUsuarioSlim(Usuario usuario) {
        return new DtUsuarioSlim(usuario.getId(), usuario.getCorreo(), usuario.getNombre(), usuario.getApellido(), usuario.getEstado());

    }
}
