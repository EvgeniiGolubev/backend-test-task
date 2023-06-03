package com.example.social_media_api.security.jwt;

import com.example.social_media_api.exception.JwtAuthenticationException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    public String generateJwtToken(String username) {
        Date now = new Date();
        Date expiration = new Date(new Date().getTime() + jwtExpiration * 1000L);
        SecretKeySpec secretKeySpec = getSecretKeySpec();

        return Jwts.builder()
                .setHeaderParam("alg", "HS256")
                .setHeaderParam("typ", "JWT")
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKeySpec)
                .compact();
    }

    public Boolean validateJwtToken(String authToken) {
        try {
            SecretKeySpec secretKeySpec = getSecretKeySpec();

            Jwts.parserBuilder().setSigningKey(secretKeySpec).build().parseClaimsJws(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtAuthenticationException("Jwt token is expired or invalid", HttpStatus.UNAUTHORIZED);
        }
    }

    public String getUserNameFromJwtToken(String token) {
        SecretKeySpec secretKeySpec = getSecretKeySpec();

        return Jwts.parserBuilder()
                .setSigningKey(secretKeySpec)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    private SecretKeySpec getSecretKeySpec() {
        return new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), SignatureAlgorithm.HS256.getJcaName());
    }
}
