package com.shopnow.shopnow.service;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.Compra;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.Note;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Component
@Transactional
public class Scheduler {

    @Autowired
    CompraRepository compraRepository;

    @Autowired
    GoogleSMTP googleSMTP;

    @Autowired
    BraintreeUtils braintreeUtils;

    @Autowired
    FirebaseMessagingService firebaseMessagingService;


    @Scheduled(cron = "0 0 */6 * * *")
    public void cancelarComprasTest() throws FirebaseMessagingException, FirebaseAuthException {
        List<UUID> comprasId = compraRepository.comprasInactivas();
        List<Compra> compras = compraRepository.findAllById(comprasId);

        for (Compra compra : compras) {
            compra.setEstado(EstadoCompra.Cancelada);
            Generico comprador = compraRepository.obtenerComprador(compra.getId());
            Generico vendedor = compraRepository.obtenerVendedor(compra.getId());

            String nombreParaMostrar;
            if (vendedor.getDatosVendedor().getNombreEmpresa() != null)
                nombreParaMostrar = vendedor.getDatosVendedor().getNombreEmpresa();
            else
                nombreParaMostrar = vendedor.getNombre() + " " + vendedor.getApellido();

            boolean success = braintreeUtils.devolverDinero(compra.getIdTransaccion());
            if (!success) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "No se puede realizar la devolución del dinero");
            }
            compraRepository.save(compra);
            Note noteComprador;
            String mensaje;
            noteComprador = new Note("Compra cancelada", "La compra hecha a " + nombreParaMostrar + " a sido cancelada!!! Revisa tu correo para conocer el motivo." + vendedor.getCorreo() + "", new HashMap<>(), null);
            mensaje = "La compra hecha a " + nombreParaMostrar + " a sido cancelada (Identificador: " + compra.getId() + ")!!!\nMotivo:\nSe ha excedido el tiempo de espera para confirmación de 48hrs.\n\nPara más información ponerse en contacto con el vendedor:\nCorreo: " + vendedor.getCorreo() + ".";
            String mensajeVendedor = "La venta hecha a " + comprador.getNombre() + " " + comprador.getApellido() + " a sido cancelada (Identificador: " + compra.getId() + ")!!!\nMotivo:\n\nSe ha excedido el tiempo de espera para confirmación de 48hrs.\nSi ya no utiliza ShopNow por favor, cancele la visibilidad de sus productos.";

            if (!comprador.getWebToken().equals(""))
                firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getWebToken());
            if (!comprador.getMobileToken().equals(""))
                firebaseMessagingService.enviarNotificacion(noteComprador, comprador.getMobileToken());
            googleSMTP.enviarCorreo(comprador.getCorreo(), mensaje, "Estado de compra actualizado");
            googleSMTP.enviarCorreo(vendedor.getCorreo(), mensajeVendedor, "Estado de venta actualizado");
        }

    }
}
