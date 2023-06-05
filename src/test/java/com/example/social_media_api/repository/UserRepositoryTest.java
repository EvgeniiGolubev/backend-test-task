package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindByEmail() {
        String email = "test@example.com";
        User user = new User();
        when(userRepository.findByEmail(email)).thenReturn(user);

        User result = userRepository.findByEmail(email);

        assertEquals(user, result);
    }

    @Test
    public void testFindByName() {
        String name = "testuser";
        User user = new User();
        when(userRepository.findByName(name)).thenReturn(user);

        User result = userRepository.findByName(name);

        assertEquals(user, result);
    }

    @Test
    public void testSave() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        User result = userRepository.save(user);

        assertEquals(user, result);
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        Optional<User> user = Optional.of(new User());
        when(userRepository.findById(id)).thenReturn(user);

        Optional<User> result = userRepository.findById(id);

        assertEquals(user, result);
    }
}