package com.shopnow.shopnow.controller;

import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtCompra;
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


    @PostMapping()
    public ResponseEntity<String> nuevaCompra(@Valid @RequestBody DtCompra datos) throws FirebaseMessagingException, FirebaseAuthException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        compraService.nuevaCompra(datos);
        if (!datos.getCorreoComprador().equals(email)) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return new ResponseEntity<>("Compra realizada con exito!!!", HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> completarCompraEnvio(@RequestParam UUID compra) throws FirebaseMessagingException, FirebaseAuthException {
        //TODO Validar que el UUID que hace la solicitud es uno de los dos involucradors (vendedor o comprador)
        compraService.confirmarEntregaoReciboProducto(compra);
        return new ResponseEntity<>("Compra cambiada de estado con exito!!!", HttpStatus.OK);
    }
}
