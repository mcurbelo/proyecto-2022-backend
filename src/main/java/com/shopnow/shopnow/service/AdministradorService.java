package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.*;
import com.shopnow.shopnow.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;

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

    @Autowired
    UtilService utilService;

    @Autowired
    ReclamoRepository reclamoRepository;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

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

        if (!aceptar && motivo == null) {
            throw new Excepcion("Se debe ingresar un motico si se rechaza la solicitud");
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
            producto.getImagenesURL().clear();
            productoRepository.saveAndFlush(producto);
            productoRepository.eliminarProductoCategoria(idProducto);
            productoRepository.delete(producto);
            datosVendedorRepository.deleteById(datosVendedorViejo);
            googleSMTP.enviarCorreo(usuario.getCorreo(), "Su solicitud para convertirse en vendedor ha sido rechazada, motivo:\n\n" + motivo + "\n\nPara mas información comunicarse con el soporte de la página.", "Solicitud de vendedor rechazada - ShopNow");
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
                .estado(EstadoUsuario.Activo)
                .imagen("")
                .fechaRegistro(new Date())
                .build();
        usuarioRepository.save(administrador);
        googleSMTP.enviarCorreo(administrador.getCorreo(), "Se ha creado su cuenta de administrador con los siguientes datos de inicio de sesión\n\nCorreo: " + datos.getCorreo() + "\nContraseña: " + contrasena + "", "Nueva cuenta de adminstrador - ShopNow");
    }


    public Map<String, Object> listadoSolicitudes(int pageNo, int pageSize, String sortBy, String sortDir) {

        if (!sortBy.matches("id")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<DatosVendedor> solicitudes = datosVendedorRepository.findByEstadoSolicitud(EstadoSolicitud.Pendiente, pageable);


        List<DatosVendedor> listaDeUsuarios = solicitudes.getContent();

        List<DtSolicitudPendiente> content = listaDeUsuarios.stream().map(this::getDtSolicitudPendiente).toList();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("solicitudes", content);
        response.put("currentPage", solicitudes.getNumber());
        response.put("totalItems", solicitudes.getTotalElements());
        response.put("totalPages", solicitudes.getTotalPages());

        return response;
    }


    public void deshacerCompra(UUID idCompra) throws FirebaseMessagingException, FirebaseAuthException {
        Compra compra = compraRepository.findById(idCompra).orElseThrow(() -> new Excepcion("La compra/venta no existe."));

        if (compra.getEstado() == EstadoCompra.Devolucion || compra.getEstado() == EstadoCompra.Cancelada) {
            throw new Excepcion("Esta compra/venta no se puede reembolsar. Ya que la compra no está en un estado valído.");
        }

        if (!reclamoRepository.existsByCompraAndResuelto(compra, TipoResolucion.NoResuelto)) {
            throw new Excepcion("Esta compra/venta no se puede reembolsar. Se necesita un reclamo en estado de 'No resuelto'");
        }
        braintreeUtils.devolverDinero(compra.getIdTransaccion());
        compra.setEstado(EstadoCompra.Devolucion);
        compraRepository.save(compra);

        List<Reclamo> reclamos = reclamoRepository.findByCompra(compra);

        for (Reclamo reclamo : reclamos) {
            if (reclamo.getResuelto() == TipoResolucion.NoResuelto) {
                reclamo.setResuelto(TipoResolucion.Devolucion);
            }
        }
        reclamoRepository.saveAll(reclamos);

        Generico comprador = compraRepository.obtenerComprador(compra.getId());
        Generico vendedor = compraRepository.obtenerVendedor(compra.getId());

        if (!comprador.getWebToken().equals("")) {
            Note notificacionComprador = new Note("Compra reembolsada", "Una de tus compras ha sido reembolsada por un administrador.", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(notificacionComprador, comprador.getWebToken());
        }
        if (!comprador.getMobileToken().equals("")) {
            Note notificacionComprador = new Note("Compra reembolsada", "Una de tus compras ha sido reembolsada por un administrador.", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(notificacionComprador, comprador.getWebToken());
        }
        googleSMTP.enviarCorreo(comprador.getCorreo(), "Hola, " + comprador.getNombre() + " " + comprador.getApellido() + ".\nLa compra (identificador: " + compra.getId() + ") ha sido reembolsada por un administrador. Ya se ha iniciado la transacción de devolución de dinero. Detalles:\n" + utilService.detallesCompra(compra, vendedor, comprador, compra.getInfoEntrega().getProducto(), compra.getInfoEntrega().getEsEnvio()), "Compra reembolsada - ShopNow");

        if (!vendedor.getWebToken().equals("")) {
            Note notificacionVendedor = new Note("Venta reembolsada", "Una de tus ventas ha sido reembolsada por un administrador", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(notificacionVendedor, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + vendedor.getNombre() + " " + vendedor.getApellido() + ".\nLa venta (identificador: " + compra.getId() + ") ha sido reembolsada por un administrador. Ya se ha iniciado la transacción de devolución de dinero. Detalles:\n" + utilService.detallesCompra(compra, vendedor, comprador, compra.getInfoEntrega().getProducto(), compra.getInfoEntrega().getEsEnvio()), "Venta reembolsada - ShopNow");
    }


    public Map<String, Object> estadisticaUsuarios(Date fechaInicio, Date fechaFin, Boolean historico) {
        List<Generico> usuarios;
        List<Administrador> administradores;
        int cantidadVendedores = 0, cantidadSoloCompradores = 0, cantidadActivos = 0, cantidadBloqueados = 0, cantidadEliminados = 0,
                cantidadAdministradores = 0, cantidadAdmActivos = 0, cantidadAdmBloqueados = 0, cantidadAdmEliminados = 0;
        Map<String, Object> response = new LinkedHashMap<>();
        int total = usuarioRepository.totalUsuarios();

        if (historico) {
            usuarios = usuarioRepository.usuariosSistema();
            administradores = usuarioRepository.administradoresSistema();
        } else {
            usuarios = usuarioRepository.usuariosSistemaRango(fechaInicio, fechaFin);
            administradores = usuarioRepository.administradoresSistemaRango(fechaInicio, fechaFin);

        }
        response.put("total", total);
        response.put("muestra", usuarios.size() + administradores.size());

        for (Generico usuario : usuarios) {
            if (usuario.getDatosVendedor() == null || usuario.getDatosVendedor().getEstadoSolicitud() != EstadoSolicitud.Aceptado) {
                cantidadSoloCompradores++;
            }
            if (usuario.getDatosVendedor() != null && usuario.getDatosVendedor().getEstadoSolicitud() == EstadoSolicitud.Aceptado) {
                cantidadVendedores++;
            }

            if (usuario.getEstado() == EstadoUsuario.Activo)
                cantidadActivos++;
            else if (usuario.getEstado() == EstadoUsuario.Bloqueado)
                cantidadBloqueados++;
            else
                cantidadEliminados++;
        }

        DtUsuarioEst usuariosEst = new DtUsuarioEst(cantidadVendedores, cantidadSoloCompradores, cantidadActivos, cantidadBloqueados, cantidadEliminados);
        response.put("usuarios", usuariosEst);

        for (Administrador administrador : administradores) {
            if (administrador.getEstado() == EstadoUsuario.Activo)
                cantidadAdmActivos++;
            else if (administrador.getEstado() == EstadoUsuario.Bloqueado)
                cantidadAdmBloqueados++;
            else
                cantidadAdmEliminados++;
        }

        DtAdminEst usuariosAdm = new DtAdminEst(cantidadAdministradores, cantidadAdmActivos, cantidadAdmBloqueados, cantidadAdmEliminados);
        response.put("admins", usuariosAdm);


        return response;
    }

    public Map<String, Object> estaditicasVentas(Date fechaInicio, Date fechaFin, Boolean historico) {
        List<Compra> compras;
        int cantidadCompletadas = 0, cantidadCancelados = 0, cantidadRembolsada = 0, cantidadAceptada = 0, cantidadPendiente = 0, total = 0;
        total = compraRepository.totalCompras();


        if (historico) {
            compras = compraRepository.findAll();
        } else {
            compras = compraRepository.comprasPorRango(fechaInicio, fechaFin);


        }
        for (Compra compra : compras) {
            if (compra.getEstado() == EstadoCompra.Completada)
                cantidadCompletadas++;
            else if (compra.getEstado() == EstadoCompra.Cancelada)
                cantidadCancelados++;
            else if (compra.getEstado() == EstadoCompra.Devolucion)
                cantidadRembolsada++;
            else if (compra.getEstado() == EstadoCompra.Confirmada)
                cantidadAceptada++;
            else
                cantidadPendiente++;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("completadas", cantidadCompletadas);
        response.put("canceladas", cantidadCancelados);
        response.put("reembolsadas", cantidadRembolsada);
        response.put("aceptadas", cantidadAceptada);
        response.put("pendientes", cantidadPendiente);
        response.put("total", total);
        response.put("muestra", compras.size());
        return response;
    }

    public Map<String, Object> estadisticasReclamos(Date fechaInicio, Date fechaFin, Boolean historico) {
        List<Reclamo> reclamos;
        int resueltosPorChat = 0, resueltosPorDevolucion = 0, noResueltos = 0,
                tipoDesperfecto = 0, tipoRepeticionIncoveniente = 0, tipoProductoNoRecibo = 0,
                tipoProductoErroneo = 0, tipoOtro = 0, total = 0;
        total = reclamoRepository.totalReclamos();

        if (historico) {
            reclamos = reclamoRepository.findAll();
        } else {
            reclamos = reclamoRepository.reclamosPorRango(fechaInicio, fechaFin);
        }
        for (Reclamo reclamo : reclamos) {
            if (reclamo.getResuelto() == TipoResolucion.Devolucion)
                resueltosPorDevolucion++;
            if (reclamo.getResuelto() == TipoResolucion.NoResuelto)
                noResueltos++;
            if (reclamo.getResuelto() == TipoResolucion.PorChat)
                resueltosPorChat++;
            if (reclamo.getTipo() == TipoReclamo.Otro)
                tipoOtro++;
            if (reclamo.getTipo() == TipoReclamo.DesperfectoProducto)
                tipoDesperfecto++;
            if (reclamo.getTipo() == TipoReclamo.RepticionIncoveniente)
                tipoRepeticionIncoveniente++;
            if (reclamo.getTipo() == TipoReclamo.ProductoNoRecibido)
                tipoProductoNoRecibo++;
            if (reclamo.getTipo() == TipoReclamo.ProducoErroneo)
                tipoProductoErroneo++;
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("resueltosChat", resueltosPorChat);
        response.put("resueltosDevolucion", resueltosPorDevolucion);
        response.put("noResueltos", noResueltos);
        response.put("tipoDesperfecto", tipoDesperfecto);
        response.put("tipoRepeticion", tipoRepeticionIncoveniente);
        response.put("tipoProductoNoRecibo", tipoProductoNoRecibo);
        response.put("tipoProductoErroneo", tipoProductoErroneo);
        response.put("tipoOtro", tipoOtro);
        response.put("muestra", reclamos.size());
        response.put("total", total);

        return response;
    }


    private DtSolicitudPendiente getDtSolicitudPendiente(DatosVendedor datosVendedor) {
        Generico solicitante = datosVendedorRepository.obtenerSolicitante(datosVendedor.getId());
        Producto productoBase = solicitante.getProductos().values().stream().findFirst().orElseThrow(() -> new Excepcion("Error obteniendo la informacion de la solicitud."));
        DtMiProducto productoInfo = utilService.generarDtMiProductos(productoBase);

        Map<UUID, Calificacion> calificaciones = solicitante.getCalificaciones();
        float sumaCalificacionComprador = 0, calificacionComprador = 0;
        if (calificaciones.size() != 0) {
            for (Calificacion calificacion : calificaciones.values()) {
                sumaCalificacionComprador += calificacion.getPuntuacion();
            }
            calificacionComprador = sumaCalificacionComprador / calificaciones.size();
        }
        Direccion local = datosVendedor.getLocales().values().stream().findFirst().orElseThrow(() -> new Excepcion("Error obteniendo la informacion de la solicitud."));

        return new DtSolicitudPendiente(datosVendedor.getId(), solicitante.getId(), productoInfo, solicitante.getNombre() + " " + solicitante.getApellido(),
                calificacionComprador, solicitante.getImagen(), solicitante.getCorreo(), solicitante.getTelefono(),
                datosVendedor.getNombreEmpresa(), datosVendedor.getTelefonoEmpresa(), datosVendedor.getRut(), local.toString());
    }
}
