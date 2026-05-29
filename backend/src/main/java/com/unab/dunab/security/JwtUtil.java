package com.unab.dunab.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${dunab.jwt.secret}")
    private String secret;

    @Value("${dunab.jwt.expiration-ms}")
    private long expirationMs;

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    /**
     * Genera un JWT con el correo como subject y el rol como claim adicional.
     * Usado por AuthService al registrar / iniciar sesión.
     */
    public String generateToken(String correo, String rol) {
        return Jwts.builder()
                .subject(correo)
                .claims(Map.of("rol", rol))
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getKey())
                .compact();
    }

    /** Alias sin rol (uso interno). */
    public String generarToken(String correo) {
        return generateToken(correo, "ESTUDIANTE");
    }

    public String extraerCorreo(String token) {
        return Jwts.parser().verifyWith(getKey()).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public boolean esValido(String token) {
        try {
            Jwts.parser().verifyWith(getKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
