package com.shopnow.shopnow.controller;



import com.shopnow.shopnow.model.datatypes.DtDatosLogin;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("/registrarse")
    public Map<String, String> registerHandler(@RequestBody DtUsuario datosUsuario){
         return authService.registrarUsuario(datosUsuario);
    }

    @PostMapping("/iniciarSesion")
    public Map<String, String> loginHandler(@RequestBody DtDatosLogin datos){
        return authService.iniciarSesion(datos.getCorreo(), datos.getPassword());
    }


}