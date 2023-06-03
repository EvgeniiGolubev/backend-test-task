package com.example.social_media_api.security.jwt;

import com.example.social_media_api.exception.JwtAuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {
    private final UserDetailsService userDetailsService;
    private final JwtTokenProvider jwtProvider;

    @Value("${jwt.cookieName}")
    private String authCookieName;

    @Autowired
    public JwtTokenFilter(@Qualifier("userDetailsServiceImpl") UserDetailsService userDetailsService,
                          JwtTokenProvider jwtProvider) {
        this.userDetailsService = userDetailsService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        try {
            if (token != null && jwtProvider.validateJwtToken(token)) {
                String username = jwtProvider.getUserNameFromJwtToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                Authentication authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (JwtAuthenticationException e) {
            SecurityContextHolder.clearContext();
            response.sendError(e.getHttpStatus().value());

            throw new JwtAuthenticationException("Jwt token is expired or invalid");
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        if (request.getCookies() == null) return null;

        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(authCookieName)) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
