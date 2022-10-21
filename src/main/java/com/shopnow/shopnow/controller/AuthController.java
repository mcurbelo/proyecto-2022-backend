package com.shopnow.shopnow.controller;


import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.datatypes.DtDatosLogin;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthService authService;


    @PostMapping("/registrarse")
    public RegistrarUsuarioResponse registerHandler(@RequestBody DtUsuario datosUsuario) {
        return authService.registrarUsuario(datosUsuario);
    }

    @PostMapping("/iniciarSesion")
    public Map<String, String> loginHandler(@RequestBody DtDatosLogin datos) {
        return authService.iniciarSesion(datos.getCorreo(), datos.getPassword());
    }

    @PostMapping("/recuperarContrasena")
    public ResponseEntity<String> recuperarContrasena(@RequestParam(value = "correo") String correo) throws NoSuchAlgorithmException {
        authService.recuperarContrasena(correo);
        return new ResponseEntity<>("Accion realizada!!!", HttpStatus.OK);
    }

    @PostMapping("/reiniciarContrasena")
    public ResponseEntity<String> reiniciarContrasena(@RequestParam(value = "token") String token, @RequestParam(value = "contrasena") String contrasena) {
        authService.reiniciarContrasena(token, contrasena);
        return new ResponseEntity<>("Contraseña cambiada con exito!!!", HttpStatus.OK);
    }


}