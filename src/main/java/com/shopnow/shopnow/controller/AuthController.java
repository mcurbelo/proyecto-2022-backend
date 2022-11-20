package com.shopnow.shopnow.controller;


import com.fasterxml.jackson.jr.ob.JSON;
import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.datatypes.DtDatosLogin;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
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
        return authService.iniciarSesion(datos.getCorreo(), datos.getPassword(), datos.getTokenWeb(), datos.getTokenMobile());
    }

    @PutMapping("/recuperarContrasena")
    public ResponseEntity<String> recuperarContrasena(@RequestParam(value = "correo") String correo) throws NoSuchAlgorithmException {
        authService.recuperarContrasena(correo);
        return new ResponseEntity<>("Accion realizada!!!", HttpStatus.OK);
    }

    @PutMapping("/reiniciarContrasena")
    public ResponseEntity<String> reiniciarContrasena(@RequestParam(value = "token") String token, @RequestBody Map<String, Object> datosContrasena) {
        authService.reiniciarContrasena(token, datosContrasena.get("contrasena").toString());
        return new ResponseEntity<>("Contraseña cambiada con exito!!!", HttpStatus.OK);
    }

    @GetMapping("/verificarCodigo")
    public ResponseEntity<String> verificarCodigo(@RequestParam(value = "codigo") String codigo) {
        try{
            authService.verificarCodigo(codigo);
            return new ResponseEntity<>("Codigo correcto", HttpStatus.OK);
        }catch (Exception e) {
            return new ResponseEntity<>("Codigo incorrecto", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}