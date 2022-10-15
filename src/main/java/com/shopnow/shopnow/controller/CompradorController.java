package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.service.CompradorService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/compradores")
public class CompradorController {
    private final String ANONYMOUS_USER = "anonymousUser";
    @Autowired
    CompradorService compradorService;

    @PostMapping("/solicitudVendedor")
    public ResponseEntity<String> nuevaSolicitud(@RequestPart DtSolicitud datos, @RequestPart final MultipartFile[] imagenes) throws IOException {
        compradorService.crearSolicitud(datos, imagenes);
        return new ResponseEntity<>("Solicitud enviada con exito!!!", HttpStatus.OK);
    }
    @PostMapping("/agregarDireccion")
    public ResponseEntity<Object> agregarDireccion(@RequestBody DtDireccion datos) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(Objects.equals(email, ANONYMOUS_USER)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        compradorService.agregarDreccion(datos, email);
        return ResponseEntity.ok().build();
    }

}
