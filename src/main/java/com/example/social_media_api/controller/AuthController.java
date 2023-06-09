package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.AuthUserDto;
import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.dto.UserDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
                    content = @Content(schema = @Schema(implementation = UserDto.class))
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
        authenticateUser(loginUserDto, response);

        UserDto userDto = userService.findUserByEmail(loginUserDto.getEmail());

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Operation(summary = "Register user", description = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
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
        userService.checkEmailExists(newUserDto.getEmail());
        userService.checkNameExists(newUserDto.getName());

        userService.saveUser(newUserDto);

        authenticateUser(newUserDto, response);

        UserDto userDto = userService.findUserByEmail(newUserDto.getEmail());

        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    private void authenticateUser(AuthUserDto loginUserDto, HttpServletResponse response) throws UserAuthenticationException {
        try {
            Authentication authentication = authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            response.addCookie(jwtUtils.makeCookie(loginUserDto.getEmail()));
        } catch (AuthenticationException e) {
            throw new UserAuthenticationException("Invalid email or password");
        }
    }
}
