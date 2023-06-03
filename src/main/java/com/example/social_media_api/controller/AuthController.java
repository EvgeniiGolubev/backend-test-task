package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.AuthUserDto;
import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.jwt.JwtUtils;
import com.example.social_media_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginUserDto loginUserDto, HttpServletResponse response) {
        try {
            authenticateUser(loginUserDto, response);

            return new ResponseEntity<>(new ResponseMessage("User success logged!"), HttpStatus.OK);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(new ResponseMessage("Invalid email or password"), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody NewUserDto newUserDto,
                                          HttpServletResponse response) {
        try {
            userService.checkEmailExists(newUserDto.getEmail());
            userService.checkNameExists(newUserDto.getName());

            userService.saveUser(newUserDto);

            authenticateUser(newUserDto, response);

            return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (AuthenticationException e) {
            return new ResponseEntity<>(new ResponseMessage("Invalid email or password"), HttpStatus.UNAUTHORIZED);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ResponseMessage>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ResponseMessage> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ResponseMessage(error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    private void authenticateUser(AuthUserDto loginUserDto, HttpServletResponse response) throws AuthenticationException {
        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        response.addCookie(jwtUtils.makeCookie(loginUserDto.getEmail()));
    }
}
