package com.shopnow.shopnow.service;

import com.shopnow.shopnow.controller.responsetypes.Excepcion;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Reclamo;
import com.shopnow.shopnow.model.datatypes.DtAltaReclamo;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.model.enumerados.TipoResolucion;
import com.shopnow.shopnow.repository.CompraRepository;
import com.shopnow.shopnow.repository.ReclamoRepository;
import com.shopnow.shopnow.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
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

    public void iniciarReclamo(DtAltaReclamo datos, UUID idCompra, UUID idComprador) {

        if (idComprador != compraRepository.obtenerComprador(idCompra).getId()) {
            throw new Excepcion("Usuario invalido");
        }
        Generico comprador = (Generico) usuarioRepository.findById(idComprador).orElseThrow();

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

        if (vendedor.getWebToken() != null) {
            // Note note= new Note()
        }
        //googleSMTP.enviarCorreo();


    }
}
