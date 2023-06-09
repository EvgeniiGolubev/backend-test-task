package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.LoginUserDto;
import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.dto.UserDto;
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
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginUserSuccessful() throws UserAuthenticationException {
        LoginUserDto user = new LoginUserDto();
        user.setEmail("test@example.com");
        user.setPassword("password");

        UserDto userDto = new UserDto();

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(user);

        when(userService.findUserByEmail(user.getEmail())).thenReturn(userDto);

        ResponseEntity<?> responseEntity = authController.loginUser(user, response);

        assertThat(violations).isEmpty();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDto, responseEntity.getBody());
    }

    @Test
    public void testLoginUserInvalidCredentials() {
        LoginUserDto user = new LoginUserDto();
        user.setEmail("test@example.com");
        user.setPassword("password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException(""));

        UserAuthenticationException exception = assertThrows(
                UserAuthenticationException.class,
                () -> authController.loginUser(user, response)
        );

        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    public void testRegisterUserSuccessful() throws UserAlreadyExistsException, UserAuthenticationException {
        NewUserDto user = new NewUserDto();
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setConfirmPassword("password");
        user.setName("John Doe");

        UserDto userDto = new UserDto();

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(user);

        when(userService.findUserByEmail(user.getEmail())).thenReturn(userDto);

        ResponseEntity<?> responseEntity = authController.registerUser(user, response);

        assertThat(violations).isEmpty();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDto, responseEntity.getBody());
    }

    @Test
    void loginUserWithValidFieldsInLoginUserDto() {
        LoginUserDto user = new LoginUserDto();
        user.setEmail("test@mail.ru");
        user.setPassword("password");

        UserDto userDto = new UserDto();

        Set<ConstraintViolation<LoginUserDto>> violations = validator.validate(user);

        when(userService.findUserByEmail(user.getEmail())).thenReturn(userDto);

        ResponseEntity<?> responseEntity = authController.loginUser(user, response);

        assertThat(violations).isEmpty();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDto, responseEntity.getBody());
    }

    @Test
    void registerUserWithValidFieldsInNewUserDto() {
        NewUserDto user = new NewUserDto();
        user.setEmail("test@mail.ru");
        user.setPassword("password");
        user.setConfirmPassword("password");
        user.setName("John Doe");

        UserDto userDto = new UserDto();

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(user);

        when(userService.findUserByEmail(user.getEmail())).thenReturn(userDto);

        ResponseEntity<?> responseEntity = authController.registerUser(user, response);

        assertThat(violations).isEmpty();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(userDto, responseEntity.getBody());
    }

    @Test
    void validateWrongFieldsInNewUserDto() {
        NewUserDto user = new NewUserDto();
        user.setEmail("invalidemail");
        user.setPassword("password");
        user.setConfirmPassword("anotherPassword");
        user.setName(" ");

        Set<ConstraintViolation<NewUserDto>> violations = validator.validate(user);

        assertThat(violations).hasSize(3);
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Email is not correct", "Name cannot be empty", "Passwords do not match");
    }
}