package com.example.social_media_api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;

public class UserAuthenticationException extends AuthenticationException {
    public UserAuthenticationException(String msg) {
        super(msg);
    }
}
