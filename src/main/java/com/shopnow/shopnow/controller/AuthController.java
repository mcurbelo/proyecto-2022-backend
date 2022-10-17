package com.shopnow.shopnow.controller;



import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.StorageOptions;
import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.datatypes.DtDatosLogin;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/registrarse")
    public RegistrarUsuarioResponse registerHandler(@RequestBody DtUsuario datosUsuario){
         return authService.registrarUsuario(datosUsuario);
    }

    @PostMapping("/iniciarSesion")
    public Map<String, String> loginHandler(@RequestBody DtDatosLogin datos){
        return authService.iniciarSesion(datos.getCorreo(), datos.getPassword());
    }



}