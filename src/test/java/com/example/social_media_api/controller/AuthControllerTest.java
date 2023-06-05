package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserAuthenticationException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.jwt.JwtUtils;
import com.example.social_media_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginUserSuccessful() throws UserAuthenticationException {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");
        loginUserDto.setPassword("password");

        Authentication authentication = new TestingAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);

        ResponseEntity<?> responseEntity = authController.loginUser(loginUserDto, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User successfully logged in", ((ResponseMessage) responseEntity.getBody()).getMessage());
    }

    @Test
    public void testLoginUserInvalidCredentials() {
        LoginUserDto loginUserDto = new LoginUserDto();
        loginUserDto.setEmail("test@example.com");
        loginUserDto.setPassword("password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        ResponseEntity<?> responseEntity = authController.loginUser(loginUserDto, response);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertEquals("Invalid email or password", ((ResponseMessage) responseEntity.getBody()).getMessage());
    }

    @Test
    public void testRegisterUserSuccessful() throws UserAlreadyExistsException, UserAuthenticationException {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setEmail("test@example.com");
        newUserDto.setPassword("password");
        newUserDto.setConfirmPassword("password");
        newUserDto.setName("John Doe");

        ResponseEntity<?> responseEntity = authController.registerUser(newUserDto, response);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("User registered successfully", ((ResponseMessage) responseEntity.getBody()).getMessage());
        verify(userService, times(1)).saveUser(newUserDto);
    }

    @Test
    public void testHandleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);

        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError fieldError1 = new FieldError("newUserDto", "name", "Name cannot be empty");
        FieldError fieldError2 = new FieldError("newUserDto", "confirmPassword", "Repeat password cannot be empty");
        fieldErrors.add(fieldError1);
        fieldErrors.add(fieldError2);

        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<List<ResponseMessage>> responseEntity = authController.handleValidationException(ex);

        List<ResponseMessage> responseMessages = responseEntity.getBody();

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(2, responseMessages.size());
        assertEquals("name: Name cannot be empty", responseMessages.get(0).getMessage());
        assertEquals("confirmPassword: Repeat password cannot be empty", responseMessages.get(1).getMessage());
    }
}