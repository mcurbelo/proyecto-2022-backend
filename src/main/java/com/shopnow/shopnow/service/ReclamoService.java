package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.*;
import com.shopnow.shopnow.model.datatypes.*;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.ReclamoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReclamoService {

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    ReclamoRepository reclamoRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;

    @Autowired
    BraintreeUtils braintreeUtils;

    public void iniciarReclamo(DtAltaReclamo datos, UUID idCompra, UUID idComprador, String correoComprador) throws FirebaseMessagingException, FirebaseAuthException {

        Generico comprador = (Generico) usuarioRepository.findByIdAndEstado(idComprador, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("Usuario inhabilitado"));

        if (!correoComprador.equals(comprador.getCorreo())) {
            throw new Excepcion("Usuario invalido");
        }

        if (idComprador.compareTo(comprador.getId()) != 0) {
            throw new Excepcion("Usuario invalido");
        }

        if (comprador.getEstado() != EstadoUsuario.Activo) {
            throw new Excepcion("El usuario comprador esta inhabilitado");
        }

        Generico vendedor = compraRepository.obtenerVendedor(idCompra);

        if (vendedor.getEstado() != EstadoUsuario.Activo) {
            throw new Excepcion("El usuario vendedor esta inhabilitado");
        }
        Compra compra = compraRepository.findById(idCompra).orElseThrow();

        if (compra.getEstado() == EstadoCompra.Cancelada || compra.getEstado() == EstadoCompra.EsperandoConfirmacion) {
            throw new Excepcion("A esta compra no se le pueden realizar reclamos");
        }

        if (reclamoRepository.existsByCompraAndResuelto(compra, TipoResolucion.NoResuelto)) {
            throw new Excepcion("Solo se puede tener un reclamo activo por compra");
        }

        Integer diasGarantia = compra.getInfoEntrega().getProducto().getDiasGarantia();
        Calendar cal = Calendar.getInstance();
        cal.setTime(ObjectUtils.firstNonNull(compra.getInfoEntrega().getHorarioRetiroLocal(), compra.getInfoEntrega().getTiempoEstimadoEnvio()));
        cal.add(Calendar.DATE, diasGarantia);
        Date fechaLimite = cal.getTime();
        if (new Date().after(fechaLimite) && compra.getEstado() == EstadoCompra.Completada) {
            throw new Excepcion("No se puede realizar un reclamo porque vencio el plazo de garantia");
        }


        Reclamo reclamo = new Reclamo(null, datos.getTipo(), new Date(), datos.getDescripcion(), TipoResolucion.NoResuelto, compra);
        reclamoRepository.saveAndFlush(reclamo);
        comprador.getReclamos().put(reclamo.getId(), reclamo);
        usuarioRepository.save(comprador);

        String nombreParaMostrar = (vendedor.getDatosVendedor().getNombreEmpresa() != null) ? vendedor.getDatosVendedor().getNombreEmpresa() : vendedor.getNombre() + " " + vendedor.getApellido();

        if (vendedor.getWebToken() != null) {
            Note note = new Note("Nuevo reclamo", "Hay un nuevo reclamo sin resolver, ve hacia la sección 'Reclamos recibidos' para mas información", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(note, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + nombreParaMostrar + ".\nTiene un nuevo reclamo en una compra (identificador: " + compra.getId() + "). Visite el sitio y vaya a la sección 'Mis reclamos' para poder realizar acciones.", "Nuevo reclamo - " + reclamo.getId() + " - ShopNow");
    }

    public void gestionReclamo(UUID idVenta, UUID idReclamo, UUID idVendedor, TipoResolucion resolucion) throws FirebaseMessagingException, FirebaseAuthException {
        Reclamo reclamo = reclamoRepository.findById(idReclamo).orElseThrow(() -> new Excepcion("No existe el reclamo"));
        Compra compra = compraRepository.findById(idVenta).orElseThrow(() -> new Excepcion("No existe la compra"));
        Generico vendedor = (Generico) usuarioRepository.findByIdAndEstado(idVendedor, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("Usuario inhabilitado"));
        Generico comprador = compraRepository.obtenerComprador(idVenta);

        if (reclamo.getResuelto() != TipoResolucion.NoResuelto) {
            throw new Excepcion("Este reclamo ya se solucionó");
        }

        if (resolucion == TipoResolucion.NoResuelto) {
            throw new Excepcion("Resolucion inválida");
        }
        //Logica
        Note notificacionComprador;
        if (resolucion == TipoResolucion.Devolucion) {
            boolean success = braintreeUtils.devolverDinero(compra.getIdTransaccion());
            if (!success) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se puede realizar la devolucion de dinero");
            }
            reclamo.setResuelto(TipoResolucion.Devolucion);
            reclamoRepository.save(reclamo);
            compra.setEstado(EstadoCompra.Devolucion);
            compraRepository.save(compra);
            if (comprador.getWebToken() != null) {
                notificacionComprador = new Note("Reclamo resuelto: Devolución", "Uno de tus reclamos ha sido marcado como resuelto, ve a 'Reclamos recibidos' para obtener mas información.", new HashMap<>(), "");
                firebaseMessagingService.enviarNotificacion(notificacionComprador, comprador.getWebToken());
            }
            googleSMTP.enviarCorreo(comprador.getCorreo(), "Hola, " + comprador.getNombre() + " " + comprador.getApellido() + ".\nEl reclamo hacia la compra (identificador:" + idVenta + ") ha sido marcado como resuelto vía devolución de dinero.", "Reclamo resuelto - " + reclamo.getId());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Funcionalidad equivocada");

        }
    }

    public void marcarComoResuelto(UUID idCompra, UUID idReclamo, UUID idComprador, String correoComprador) throws FirebaseMessagingException, FirebaseAuthException {
        Reclamo reclamo = reclamoRepository.findById(idReclamo).orElseThrow(() -> new Excepcion("No existe el reclamo"));
        Generico vendedor = compraRepository.obtenerVendedor(idCompra);
        Generico comprador = compraRepository.obtenerComprador(idCompra);

        if (!correoComprador.equals(comprador.getCorreo())) {
            throw new Excepcion("Usuario invalido");
        }

        if (idComprador.compareTo(comprador.getId()) != 0) {
            throw new Excepcion("Usuario invalido");
        }

        if (comprador.getEstado() != EstadoUsuario.Activo) {
            throw new Excepcion("El usuario comprador esta inhabilitado");
        }

        if (reclamo.getResuelto() != TipoResolucion.NoResuelto) {
            throw new Excepcion("No se puede modificar el estado de este reclamo");
        }
        reclamo.setResuelto(TipoResolucion.PorChat);
        reclamoRepository.save(reclamo);

        String nombreParaMostrar = (vendedor.getDatosVendedor().getNombreEmpresa() != null) ? vendedor.getDatosVendedor().getNombreEmpresa() : vendedor.getNombre() + " " + vendedor.getApellido();

        if (vendedor.getWebToken() != null) {
            Note note = new Note("Reclamo resuelto", "Uno de tus reclamos ha sido marcado como resuelto por el comprador. ve a 'Mis reclamos' para obtener mas información.", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(note, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + nombreParaMostrar + ".\n El reclamo hacia la venta (identificador:" + idCompra + ") ha sido marcado como resuelto vía chat.", "Reclamo resuelto - " + reclamo.getId());
    }

    //Cambiar de lugar
    public Map<String, Object> listarMisReclamosHechos(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltroReclamo filtros, UUID id) {
        if (!sortBy.matches("fecha|resuelto|tipo")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Reclamo> reclamos;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> reclamosCumplenFiltro;
        if (filtros != null) {
            List<UUID> reclamosIdConFecha = null;
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                reclamosIdConFecha = reclamoRepository.misReclamosHechosPorFecha(id, fecha);
            }
            List<UUID> reclamoIdConTipo = null;
            if (filtros.getTipo() != null) {
                reclamoIdConTipo = reclamoRepository.misReclamosHechosPorTipo(id, filtros.getTipo().toString());
            }
            List<UUID> reclamosIdConNombreVendedor = null;
            if (filtros.getNombreUsuario() != null) {
                reclamosIdConNombreVendedor = reclamoRepository.misReclamosHechosPorNombreVendedor(id, filtros.getNombreUsuario());
            }
            List<UUID> reclamosIdConNombreProducto = null;
            if (filtros.getNombreProducto() != null) {
                reclamosIdConNombreProducto = reclamoRepository.misReclamosHechosPorNombreProducto(id, filtros.getNombreProducto());
            }
            List<UUID> reclamosIdEstado = null;
            if (filtros.getResolucion() != null) {
                reclamosIdEstado = reclamoRepository.misReclamosHechosPorResolucion(id, filtros.getResolucion().toString());
            }

            reclamosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), reclamosIdEstado, reclamosIdConNombreProducto, reclamosIdConNombreVendedor,
                    reclamoIdConTipo, reclamosIdConFecha).stream().toList();
            reclamos = reclamoRepository.findByIdIn(reclamosCumplenFiltro, pageable);
        } else
            reclamos = reclamoRepository.misReclamosHechos(id, pageable);


        List<Reclamo> listaDeReclamos = reclamos.getContent();

        List<DtReclamo> content = listaDeReclamos.stream().map(this::getDtReclamo).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reclamos", content);
        response.put("currentPage", reclamos.getNumber());
        response.put("totalItems", reclamos.getTotalElements());
        response.put("totalPages", reclamos.getTotalPages());

        return response;
    }

    public Map<String, Object> listarMisReclamosRecibidos(int pageNo, int pageSize, String sortBy, String sortDir, DtFiltroReclamo filtros, UUID id) {
        if (!sortBy.matches("fecha|resuelto|tipo")) {
            throw new Excepcion("Atributo de ordenamiento invalido");
        }
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();

        // create Pageable instance
        Page<Reclamo> reclamos;
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        List<UUID> reclamosCumplenFiltro;

        if (filtros != null) {
            List<UUID> reclamosIdConFecha = null;
            if (filtros.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String fecha = sdf.format(filtros.getFecha());
                reclamosIdConFecha = reclamoRepository.reclamosRecibidosPorFecha(id, fecha);
            }
            List<UUID> reclamoIdConTipo = null;
            if (filtros.getTipo() != null) {
                reclamoIdConTipo = reclamoRepository.reclamosRecibidosPorTipo(id, filtros.getTipo().toString());
            }
            List<UUID> reclamosRecibidosPorNombreComprador = null;
            if (filtros.getNombreUsuario() != null) {
                reclamosRecibidosPorNombreComprador = reclamoRepository.reclamosRecibidosPorNombreComprador(id, filtros.getNombreUsuario());
            }
            List<UUID> reclamosIdConNombreProducto = null;
            if (filtros.getNombreProducto() != null) {
                reclamosIdConNombreProducto = reclamoRepository.reclamosRecibosPorNombreProducto(id, filtros.getNombreProducto());
            }
            List<UUID> reclamosIdEstado = null;
            if (filtros.getResolucion() != null) {
                reclamosIdEstado = reclamoRepository.reclamosRecibidosPorEstado(id, filtros.getResolucion().toString());
            }

            reclamosCumplenFiltro = UtilService.encontrarInterseccion(new HashSet<>(), reclamosIdEstado, reclamosIdConNombreProducto, reclamosRecibidosPorNombreComprador,
                    reclamoIdConTipo, reclamosIdConFecha).stream().toList();
            reclamos = reclamoRepository.findByIdIn(reclamosCumplenFiltro, pageable);
        } else
            reclamos = reclamoRepository.misReclamosRecibidos(id, pageable);


        List<Reclamo> listaDeReclamos = reclamos.getContent();

        List<DtReclamo> content = listaDeReclamos.stream().map(this::getDtReclamo).collect(Collectors.toList());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("reclamos", content);
        response.put("currentPage", reclamos.getNumber());
        response.put("totalItems", reclamos.getTotalElements());
        response.put("totalPages", reclamos.getTotalPages());

        return response;
    }

    private DtReclamo getDtReclamo(Reclamo reclamo) {
        Compra compra = reclamo.getCompra();
        Producto producto = compra.getInfoEntrega().getProducto();
        CompraProducto infoEntrega = compra.getInfoEntrega();

        Date fechaEntrega = ObjectUtils.firstNonNull(infoEntrega.getHorarioRetiroLocal(), infoEntrega.getTiempoEstimadoEnvio());

        Generico vendedor = compraRepository.obtenerVendedor(compra.getId());
        Generico comprador = compraRepository.obtenerComprador(compra.getId());
        String nombreProducto = producto.getNombre();
        String nombreParaMostrar = (vendedor.getDatosVendedor().getNombreEmpresa() != null) ? vendedor.getDatosVendedor().getNombreEmpresa() : vendedor.getNombre() + " " + vendedor.getApellido();
        DtInfoCompra infoCompra = new DtInfoCompra(compra.getId(), vendedor.getId(), nombreParaMostrar, nombreProducto, compra.getInfoEntrega().getCantidad(),
                compra.getFecha(), compra.getEstado(), infoEntrega.getPrecioTotal(),
                infoEntrega.getPrecioUnitario(),
                fechaEntrega, infoEntrega.getDireccionEnvioORetiro().toString(), infoEntrega.getEsEnvio(), vendedor.getImagen(), comprador.getImagen(), producto.getImagenesURL().get(0).getUrl());

        boolean tieneChat = compra.getIdChat() != null;

        return new DtReclamo(infoCompra, reclamo.getTipo(), reclamo.getResuelto(), reclamo.getFecha(), comprador.getNombre() + " " + comprador.getApellido(), reclamo.getId(), reclamo.getDescripcion(), tieneChat);
    }

}