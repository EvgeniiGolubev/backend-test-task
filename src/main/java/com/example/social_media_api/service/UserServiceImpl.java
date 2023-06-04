package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.repository.UserRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User saveUser(NewUserDto newUserDto) {

        User user = new User(
                newUserDto.getEmail(),
                passwordEncoder.encode(newUserDto.getPassword()),
                newUserDto.getName(),
                Collections.singleton(Role.USER)
        );

        return userRepository.save(user);
    }

    @Override
    public User getUserFromUserDetails(UserDetailsImpl userDetails) {
        return userRepository.findByEmail(userDetails.getUsername());
    }

    @Override
    public User findUserById(Long id) throws UserNotFoundException {
        User user = userRepository.findById(id).orElse(null);

        if (user == null) {
            throw new UserNotFoundException("User not found");
        }

        return user;
    }

    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public void checkEmailExists(String email) throws UserAlreadyExistsException {
        if (userRepository.findByEmail(email) != null) {
            throw new UserAlreadyExistsException("Email is already taken");
        }
    }

    @Override
    public void checkNameExists(String name) throws UserAlreadyExistsException {
        if (userRepository.findByName(name) != null) {
            throw new UserAlreadyExistsException("Name is already taken");
        }
    }
}
