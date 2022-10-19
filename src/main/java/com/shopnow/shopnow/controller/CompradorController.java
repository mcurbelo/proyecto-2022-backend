package com.shopnow.shopnow.controller;


import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.shopnow.shopnow.model.datatypes.DtAltaReclamo;
import com.shopnow.shopnow.model.datatypes.DtDireccion;
import com.shopnow.shopnow.model.datatypes.DtFiltrosCompras;
import com.shopnow.shopnow.model.datatypes.DtSolicitud;
import com.shopnow.shopnow.service.CompradorService;
import com.shopnow.shopnow.service.ReclamoService;
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

    @Autowired
    ReclamoService reclamoService;

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

    @PostMapping("/{id}/compras/{idCompra}/reclamos")
    public ResponseEntity<String> iniciarReclamo(@PathVariable(value = "id") UUID idComprador, @PathVariable(value = "idCompra") UUID idCompra, @RequestBody DtAltaReclamo datos) throws FirebaseMessagingException, FirebaseAuthException {
        //TODO Verificar que coincidan los id
        reclamoService.iniciarReclamo(datos, idCompra, idComprador);
        return new ResponseEntity<>("Reclamo realizado con exito!!!", HttpStatus.OK);
    }

    @PutMapping("/{id}/compras/{idCompra}/reclamos/{idReclamo}")
    public ResponseEntity<String> reclamoResuelto(@PathVariable(value = "id") UUID idComprador, @PathVariable(value = "idCompra") UUID idCompra, @PathVariable(value = "idReclamo") UUID idReclamo) throws FirebaseMessagingException, FirebaseAuthException {
        //TODO Verificar que coincidan los id
        reclamoService.marcarComoResuelto(idCompra, idReclamo, idComprador);
        return new ResponseEntity<>("Reclamo resuelto con exito!!!", HttpStatus.OK);
    }


}
