package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Note;
import com.shopnow.shopnow.model.Reclamo;
import com.shopnow.shopnow.model.datatypes.DtAltaReclamo;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.ReclamoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

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

    public void iniciarReclamo(DtAltaReclamo datos, UUID idCompra, UUID idComprador) throws FirebaseMessagingException, FirebaseAuthException {

        Generico comprador = (Generico) usuarioRepository.findByIdAndEstado(idComprador, EstadoUsuario.Activo).orElseThrow(() -> new Excepcion("Usuario inhabilitado"));
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

        Integer diasGarantia = compra.getInfoEntrega().getProducto().getDiasGarantia();
        Calendar cal = Calendar.getInstance();
        cal.setTime(compra.getFecha());
        cal.add(Calendar.DATE, diasGarantia);
        Date fechaLimite = cal.getTime();
        if (new Date().after(fechaLimite)) {
            throw new Excepcion("No se puede realizar un reclamo porque vencio el plazo de garantia");
        }
        //TODO solo un reclamo no resuelto activo por compra?

        Reclamo reclamo = new Reclamo(null, datos.getTipo(), new Date(), datos.getDescripcion(), TipoResolucion.NoResuelto, compra);
        reclamoRepository.saveAndFlush(reclamo);
        comprador.getReclamos().put(reclamo.getId(), reclamo);
        usuarioRepository.save(comprador);

        String nombreParaMostrar = (vendedor.getDatosVendedor().getNombreEmpresa() != null) ? vendedor.getDatosVendedor().getNombreEmpresa() : vendedor.getNombre() + " " + vendedor.getApellido();

        if (!vendedor.getWebToken().equals("")) {
            Note note = new Note("Nuevo reclamo", "Hay un nuevo reclamo sin resolver, ve hacia la sección 'Mis reclamos' para mas información", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(note, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + nombreParaMostrar + ".\n Tiene un nuevo reclamo en una compra (identificador:" + compra.getId() + ") Visite el sitio y vaya a la sección 'Mis reclamos' para poder realizar acciones.", "Nuevo reclamo - " + reclamo.getId());
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
            //TODO hacer devolucion Braintree
            reclamo.setResuelto(TipoResolucion.Devolucion);
            reclamoRepository.save(reclamo);
            if (!comprador.getWebToken().equals("")) {
                notificacionComprador = new Note("Reclamo resuelto: Devolucion", "Uno de tus reclamos ah sido marcado como resuelto, ve a 'Mis reclamos' para obtener mas información.", new HashMap<>(), "");
                firebaseMessagingService.enviarNotificacion(notificacionComprador, comprador.getWebToken());
            }
            googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + comprador.getNombre() + " " + comprador.getApellido() + ".\n El reclamo hacia la compra (identificador:" + idVenta + ") ha sido marcado como resuelto vía devolución de dinero.", "Reclamo resuelto - " + reclamo.getId());
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Funcionalidad no implementada");
            //Todo Notificacion al comprador
        }
    }

    public void marcarComoResuelto(UUID idCompra, UUID idReclamo, UUID idComprador) throws FirebaseMessagingException, FirebaseAuthException {
        Reclamo reclamo = reclamoRepository.findById(idReclamo).orElseThrow(() -> new Excepcion("No existe el reclamo"));
        Generico vendedor = compraRepository.obtenerVendedor(idCompra);
        Generico comprador = compraRepository.obtenerComprador(idCompra);

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

        if (!vendedor.getWebToken().equals("")) {
            Note note = new Note("Reclamo resuelto", "Uno de tus reclamos ha sido marcado como resuelto por el comprador. ve a 'Mis reclamos' para obtener mas información.", new HashMap<>(), "");
            firebaseMessagingService.enviarNotificacion(note, vendedor.getWebToken());
        }
        googleSMTP.enviarCorreo(vendedor.getCorreo(), "Hola, " + nombreParaMostrar + ".\n El reclamo hacia la venta (identificador:" + idCompra + ") ha sido marcado como resuelto vía chat.", "Reclamo resuelto - " + reclamo.getId());
    }
}
