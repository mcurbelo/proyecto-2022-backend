package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtFiltrosCompras;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.service.CompradorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;




@RestController
@RequestMapping("/api/compradores")
public class CompradorController {
    private final String ANONYMOUS_USER = "anonymousUser";
    @Autowired
    CompradorService compradorService;

    @PostMapping("/solicitudVendedor")
    public ResponseEntity<String> nuevaSolicitud(@Valid @RequestPart DtSolicitud datos, @RequestPart final MultipartFile[] imagenes) throws IOException {
        compradorService.crearSolicitud(datos, imagenes);
        return new ResponseEntity<>("Solicitud enviada con exito!!!", HttpStatus.OK);
    }

    @PostMapping("/agregarDireccion")
    public ResponseEntity<Object> agregarDireccion(@Valid @RequestBody DtDireccion datos) {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Objects.equals(email, ANONYMOUS_USER)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        compradorService.agregarDireccion(datos, email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/compras")
    public Map<String, Object> obtenerCompras(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "20", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "fecha", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestBody(required = false) DtFiltrosCompras filtros,
            @PathVariable(value = "id") UUID id) {
        return compradorService.historialDeCompras(pageNo, pageSize, sortBy, sortDir, filtros, id);

    }

}
