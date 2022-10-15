package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.datatypes.DtCompra;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/compras")
public class ComprasController {


    @PostMapping()
    public ResponseEntity<String> nuevaCompra(@Valid @RequestBody DtCompra datos) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!datos.getCorreoComprador().equals(email)) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return new ResponseEntity<>("Compra realizada con exito", HttpStatus.OK);
    }
}
