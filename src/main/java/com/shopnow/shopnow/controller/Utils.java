package com.shopnow.shopnow.controller;

import com.shopnow.shopnow.model.Usuario;
import com.shopnow.shopnow.repository.UsuarioRepository;
import com.shopnow.shopnow.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@Component
public class Utils {
    @Autowired
    JWTUtil jwtUtil;
    String correoFromRequest(HttpServletRequest request) {
        String header = request.getHeader("authorization");
        if(header == null || !header.contains("Bearer")) return null;
        String token = header.split("Bearer")[1].trim();
        return jwtUtil.validateTokenAndRetrieveSubject(token);
    }
}
