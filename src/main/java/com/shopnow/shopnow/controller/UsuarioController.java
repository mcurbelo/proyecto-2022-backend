package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.datatypes.DtModificarUsuario;
import com.shopnow.shopnow.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    UsuarioService usuarioService;


    @PutMapping("/{correo}/perfil")
    public ResponseEntity<String> modificarPerfil(@PathVariable(value = "correo") String correo, @RequestPart DtModificarUsuario datos, @RequestPart(required = false) MultipartFile imagen) throws IOException {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        usuarioService.modificarDatosUsuario(correo, datos, imagen);
        return new ResponseEntity<>("Perfil editado con exito!!!", HttpStatus.OK);
    }


}
