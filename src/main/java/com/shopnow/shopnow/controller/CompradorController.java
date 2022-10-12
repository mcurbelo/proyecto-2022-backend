package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.service.CompradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/compradores")
public class CompradorController {

    @Autowired
    CompradorService compradorService;

    @PostMapping("/solicitudVendedor")
    public ResponseEntity<String> nuevaSolicitud(@RequestPart DtSolicitud datos, @RequestPart final MultipartFile[] imagenes) throws IOException {
        compradorService.crearSolicitud(datos, imagenes);
        return new ResponseEntity<>("Solicitud enviada con exito!!!", HttpStatus.OK);
    }

}
