package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.repository.UserRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void saveUserAndReturnsSameSavedUser() {
        NewUserDto newUserDto = new NewUserDto();
        newUserDto.setEmail("test@example.com");
        newUserDto.setName("John Doe");
        newUserDto.setPassword("password");
        newUserDto.setPassword("password");

        User expectedUser = new User(
                "test@example.com",
                "encodedPassword",
                "John Doe",
                Collections.singleton(Role.USER)
        );

        when(passwordEncoder.encode(newUserDto.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);

        User result = userService.saveUser(newUserDto);

        assertEquals(expectedUser, result);

        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordEncoder, times(1)).encode(any(String.class));
    }

    @Test
    void getSameUserFromUserDetailsImpl() {
        UserDetailsImpl userDetails = new UserDetailsImpl();
        userDetails.setUsername("test@example.com");
        userDetails.setPassword("encodedPassword");
        userDetails.setAuthorities(Collections.singleton(Role.USER));

        User expectedUser = new User(
                "test@example.com",
                "encodedPassword",
                "John Doe",
                Collections.singleton(Role.USER)
        );

        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(expectedUser);

        User result = userService.getUserFromUserDetails(userDetails);

        assertEquals(expectedUser, result);
        verify(userRepository, times(1)).findByEmail(any(String.class));
    }

    @Test
    void findUserByIdAndReturnExistingUser() {
        Long userId = 1L;
        User expectedUser = new User(
                "test@example.com",
                "encodedPassword",
                "John Doe",
                Collections.singleton(Role.USER)
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

        User user = userService.findUserById(userId);

        assertEquals(expectedUser, user);
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void findUserByIdNonExistingUserAndThrowsUserNotFoundException() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.findUserById(userId)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void updateUserAndReturnsSameUpdatedUser() {
        User user = new User(
                "test@example.com",
                "encodedPassword",
                "John Doe",
                Collections.singleton(Role.USER)
        );

        User expectedUser = new User(
                "test@example.com",
                "encodedPassword",
                "Jane Doe",
                Collections.singleton(Role.USER)
        );

        when(userRepository.save(user)).thenReturn(expectedUser);

        User result = userService.updateUser(user);

        assertEquals(expectedUser, result);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void checkEmailExists() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertDoesNotThrow(() -> userService.checkEmailExists(email));
        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void checkEmailExistsAndThrowUserAlreadyExistsException() {
        String email = "test@example.com";

        when(userRepository.findByEmail(email)).thenReturn(new User());

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.checkEmailExists(email)
        );

        assertEquals("Email is already taken", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(email);
    }

    @Test
    void checkNameExists() {
        String name = "John Doe";
        when(userRepository.findByName(name)).thenReturn(null);

        assertDoesNotThrow(() -> userService.checkNameExists(name));
        verify(userRepository, times(1)).findByName(name);
    }

    @Test
    void checkNameExistsAndThrowUserAlreadyExistsException() {
        String name = "John Doe";
        when(userRepository.findByName(name)).thenReturn(new User());

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.checkNameExists(name)
        );

        assertEquals("Name is already taken", exception.getMessage());
        verify(userRepository, times(1)).findByName(name);
    }
}