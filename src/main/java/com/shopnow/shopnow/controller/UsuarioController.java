package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {


    @Autowired
    UsuarioService usuarioService;

    @GetMapping("/obtenerInfoUsuario/{correo}")
    @ResponseBody
    public DtUsuario obtenerInfoUsuario(@PathVariable String correo){
        DtUsuario usuario = usuarioService.infoUsuario(correo);
        return usuario;
    }

}
