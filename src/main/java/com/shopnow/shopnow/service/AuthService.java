package com.shopnow.shopnow.service;




import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.model.datatypes.DtUsuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
public class AuthService {
    @Autowired
    private UsuarioRepository usuarioRepo;
    @Autowired private JWTUtil jwtUtil;
    @Autowired private AuthenticationManager authManager;
    @Autowired private PasswordEncoder passwordEncoder;


    public Map<String, String> registrarUsuario(DtUsuario datosUsuario)  {
        //validaciones
        String encodedPass = passwordEncoder.encode(datosUsuario.getPassword());
        Usuario usuario = new Usuario(null,datosUsuario.getCorreo(), encodedPass); //Usuario generico falta
        usuarioRepo.save(usuario);
        String token = jwtUtil.generateToken(usuario.getCorreo());
        return Collections.singletonMap("jwt-token", token);
    }

    public Map<String, String> iniciarSesion(String correo, String password)  {
        try {
            UsernamePasswordAuthenticationToken authInputToken = new UsernamePasswordAuthenticationToken(correo, password);
            authManager.authenticate(authInputToken);
            String token = jwtUtil.generateToken(correo);
            return Collections.singletonMap("jwt-token", token);
        }catch (AuthenticationException authExc){
            throw new RuntimeException("Credenciales invalidas");
        }
    }

}