package com.shopnow.shopnow.service;


import com.shopnow.shopnow.controller.responsetypes.RegistrarUsuarioResponse;
import com.shopnow.shopnow.model.Generico;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.model.enumerados.EstadoUsuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired
    private JWTUtil jwtUtil;
    @Autowired
    private AuthenticationManager authManager;
    @Autowired
    private PasswordEncoder passwordEncoder;


    public RegistrarUsuarioResponse registrarUsuario(DtUsuario datosUsuario) {
        //validaciones
        if (usuarioRepo.findByCorreoAndEstado(datosUsuario.getCorreo(), EstadoUsuario.Activo).isPresent()) {
            return new RegistrarUsuarioResponse(false, "", "Usuario ya existente");
        }

        String encodedPass = passwordEncoder.encode(datosUsuario.getPassword());
        Generico usuario = Generico.builder()
                .fechaNac(new Date())
                .nombre(datosUsuario.getNombre())
                .apellido(datosUsuario.getApellido()).correo(datosUsuario.getCorreo())
                .estado(EstadoUsuario.Activo)
                .imagen("").mobileToken("")
                .webToken("")
                .password(encodedPass)
                .telefono(datosUsuario.getTelefono())
                .fechaNac(datosUsuario.getFechaNac())
                .build();
        usuarioRepo.save(usuario);
        String token = jwtUtil.generateToken(usuario.getCorreo());
        return new RegistrarUsuarioResponse(true, token, "");
    }

    public Map<String, String> iniciarSesion(String correo, String password) {
        if (usuarioRepo.findByCorreoAndEstado(correo, EstadoUsuario.Activo).isEmpty()) {
            throw new RuntimeException("Credenciales invalidas");
        }

        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(correo, password);
            authManager.authenticate(authInputToken);
            String token = jwtUtil.generateToken(correo);
            return Collections.singletonMap("jwt-token", token);
        } catch (AuthenticationException authExc) {
            throw new RuntimeException("Credenciales invalidas");
        }
    }

}