package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.AuthUserDto;
import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserAuthenticationException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.jwt.JwtUtils;
import com.example.social_media_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Authentication", description = "API for user authentication")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(UserService userService, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Login user", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User successfully logged in",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"User successfully logged in\" }") })
            ),
            @ApiResponse(
                    responseCode = "400", description = "Form validation error",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Email cannot be empty\" }") })
            ),
            @ApiResponse(
                    responseCode = "401", description = "User authentication error",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Invalid email or password\" }") })
            )
    })
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(
            @Parameter(schema = @Schema(implementation = LoginUserDto.class))
            @Valid @RequestBody LoginUserDto loginUserDto,

            @Parameter(hidden = true)
            HttpServletResponse response
    ) {
        try {
            authenticateUser(loginUserDto, response);

            return new ResponseEntity<>(new ResponseMessage("User successfully logged in"), HttpStatus.OK);
        } catch (UserAuthenticationException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(summary = "Register user", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"User registered successfully\" }") })
            ),
            @ApiResponse(
                    responseCode = "400", description = "Form validation error or duplicate username or email",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Name is already taken\" }") })
            ),
            @ApiResponse(
                    responseCode = "401", description = "User authentication error",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Invalid email or password\" }") })
            )
    })
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(
            @Parameter(schema = @Schema(implementation = NewUserDto.class))
            @Valid @RequestBody NewUserDto newUserDto,

            @Parameter(hidden = true)
            HttpServletResponse response
    ) {
        try {
            userService.checkEmailExists(newUserDto.getEmail());
            userService.checkNameExists(newUserDto.getName());

            userService.saveUser(newUserDto);

            authenticateUser(newUserDto, response);

            return new ResponseEntity<>(new ResponseMessage("User registered successfully"), HttpStatus.OK);
        } catch (UserAlreadyExistsException | UserAuthenticationException e) {
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

    private void authenticateUser(AuthUserDto loginUserDto, HttpServletResponse response) throws UserAuthenticationException {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            response.addCookie(jwtUtils.makeCookie(loginUserDto.getEmail()));
        } catch (AuthenticationException e) {
            throw new UserAuthenticationException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }
    }
}
