package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.InvalidCredentialsException;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.jwt.JwtUtils;
import com.example.social_media_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginUserDto loginUserDto,
                                              HttpServletResponse response) {
        try {
            User user = userService.findUserByEmail(loginUserDto.getEmail());

            if (user == null) {
                throw new InvalidCredentialsException("Invalid email or password");
            }

            response.addCookie(jwtUtils.makeCookie(user.getEmail()));

            return new ResponseEntity<>(new ResponseMessage("User success logged!"), HttpStatus.OK);
        } catch (InvalidCredentialsException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody NewUserDto newUserDto,
                                          HttpServletResponse response) {
        try {
            if (userService.findUserByEmail(newUserDto.getEmail()) != null) {
                throw new UserAlreadyExistsException("Email is already taken!");
            }

            if (userService.findUserByName(newUserDto.getName()) != null) {
                throw new UserAlreadyExistsException("Name is already taken!");
            }

            User user = userService.saveUser(newUserDto);
            response.addCookie(jwtUtils.makeCookie(user.getEmail()));

            return new ResponseEntity<>(new ResponseMessage("User registered successfully!"), HttpStatus.OK);
        } catch (UserAlreadyExistsException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ResponseMessage>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ResponseMessage> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ResponseMessage(error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
