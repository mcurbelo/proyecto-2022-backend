package com.shopnow.shopnow.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtConfirmarCompra;
import com.shopnow.shopnow.model.enumerados.EstadoCompra;
import com.shopnow.shopnow.model.enumerados.EstadoProducto;
import com.shopnow.shopnow.service.CompraService;
import com.shopnow.shopnow.service.VendedorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/vendedores")
public class VendedorController {

    @Autowired
    VendedorService vendedorService;
    @Autowired
    CompraService compraService;

    @PutMapping("/{idUsuario}/productos/{id}/estado")
    public ResponseEntity<String> cambiarEstado(@PathVariable(value = "idUsuario") UUID id, @PathVariable(value = "id") UUID idProducto, @RequestParam(value = "nuevoEstado") EstadoProducto nuevoEstado) {
        //Ese Dt se deberia utilizar tambien para editar producto

        /*TODO Utilizar cuando se utilicen al 100% los token
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!email.equals(correo)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        */
        vendedorService.cambiarEstadoProducto(idProducto, id, nuevoEstado);
        return new ResponseEntity<>("Producto cambiado de estado con exito", HttpStatus.OK);
    }

    @PutMapping("/{idUsuario}/ventas/{id}/estado")
    public ResponseEntity<String> cambiarEstadoVenta(@PathVariable(value = "idUsuario") UUID id, @PathVariable(value = "id") UUID idVenta, @RequestParam(value = "nuevoEstado") EstadoCompra nuevoEstado, @RequestBody DtConfirmarCompra info) throws FirebaseMessagingException, FirebaseAuthException {
        compraService.cambiarEstadoVenta(id, idVenta, nuevoEstado, info);
        return new ResponseEntity<>("Estado de venta cambiado con exito!!!", HttpStatus.OK);
    }


}
