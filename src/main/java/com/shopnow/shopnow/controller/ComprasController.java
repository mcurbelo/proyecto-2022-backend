package com.shopnow.shopnow.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtCalificacion;
import com.shopnow.shopnow.model.datatypes.DtCompra;
import com.shopnow.shopnow.service.CalificacionService;
import com.shopnow.shopnow.service.CompraService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/compras")
public class ComprasController {

    @Autowired
    CompraService compraService;

    @Autowired
    CalificacionService calificacionService;


    @PostMapping()
    public ResponseEntity<String> nuevaCompra(@Valid @RequestBody DtCompra datos) throws FirebaseMessagingException, FirebaseAuthException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        compraService.nuevaCompra(datos);
        if (!datos.getCorreoComprador().equals(email)) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return new ResponseEntity<>("Compra realizada con exito!!!", HttpStatus.OK);
    }

    @PutMapping("/enviadas/{id}")
    public ResponseEntity<String> completarCompraEnvio(@PathVariable(value = "id") UUID idCompra) throws FirebaseMessagingException, FirebaseAuthException {
        //TODO Validar que el UUID que hace la solicitud es uno de los dos involucradors (vendedor o comprador)
        compraService.confirmarEntregaoReciboProducto(idCompra);
        return new ResponseEntity<>("Compra cambiada de estado con exito!!!", HttpStatus.OK);
    }

    @PostMapping("/calificaciones/{id}")
    public ResponseEntity<String> realizarCalificacion(@Valid @PathVariable(value = "id") UUID idcompra, @RequestBody DtCalificacion datos) {
        calificacionService.agregarCalificacion(idcompra, datos);
        return new ResponseEntity<>("Calificacion hecha con exito!!!", HttpStatus.OK);
    }


}
