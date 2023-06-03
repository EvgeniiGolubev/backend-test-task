package com.example.social_media_api.security.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;

@Component
public class JwtUtils {
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.expiration}")
    private int jwtExpiration;

    @Value("${jwt.cookieName}")
    private String authCookieName;

    @Autowired
    public JwtUtils(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public Cookie makeCookie(String email) {
        String token = jwtTokenProvider.generateJwtToken(email);
        Cookie cookie = new Cookie(authCookieName, token);
        cookie.setMaxAge(jwtExpiration);
        // cookie.setSecure(true); // отправка куков с флагом secure возможна только по https
        cookie.setHttpOnly(true);
        cookie.setPath("/");

        return cookie;
    }
}