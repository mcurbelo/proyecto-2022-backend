package com.shopnow.shopnow.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    public String generateToken(String email, String uuid) throws IllegalArgumentException, JWTCreationException {
        return JWT.create()
                .withSubject("Detalles usuario")
                .withClaim("correo", email)
                .withClaim("UUID", uuid)
                .withIssuedAt(new Date())
                .withIssuer("ShopNow2022")
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("Detalles usuario")
                .withIssuer("ShopNow2022")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("correo").asString();
    }

}
