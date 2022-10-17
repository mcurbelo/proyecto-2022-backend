package com.shopnow.shopnow.controller;

import com.braintreegateway.BraintreeGateway;
import com.shopnow.shopnow.controller.responsetypes.CreditCardRef;
import com.shopnow.shopnow.model.Tarjeta;
import com.shopnow.shopnow.model.datatypes.DtModificarUsuario;
import com.shopnow.shopnow.model.datatypes.DtTarjeta;
import com.shopnow.shopnow.service.UsuarioService;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;
    @Autowired
    BraintreeGateway gateway;

    @PutMapping("/{id}/perfil")
    public ResponseEntity<String> modificarPerfil(@PathVariable(value = "id") UUID id, @RequestPart DtModificarUsuario datos, @RequestPart(required = false) MultipartFile imagen) throws IOException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        //TODO Para cuando utilicemos 100% los token
        //   if(!email.equals(correo)){
        //      return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        //}
        usuarioService.modificarDatosUsuario(id, datos, imagen);
        return new ResponseEntity<>("Perfil editado con exito!!!", HttpStatus.OK);
    }
    @PostMapping("/{id}/tarjetas")
    public ResponseEntity<String> agregarTarjeta(@PathVariable(value = "id") UUID id, @RequestBody DtTarjeta tarjeta) {
        usuarioService.agregarTarjeta(tarjeta, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/tarjetas")
    public ResponseEntity<List<CreditCardRef>> fetchTarjetas(@PathVariable(value = "id") UUID id) {
        return ResponseEntity.ok().body(usuarioService.getTarjetas(id));
    }
}
