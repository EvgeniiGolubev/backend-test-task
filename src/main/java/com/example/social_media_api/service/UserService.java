package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;

public interface UserService {
    User saveUser(NewUserDto newUserDto);

    User getUserFromUserDetails(UserDetailsImpl userDetails);

    User findUserById(Long id) throws UserNotFoundException;

    User updateUser(User user);

    void checkEmailExists(String email) throws UserAlreadyExistsException;

    void checkNameExists(String name) throws UserAlreadyExistsException;

    UserDto findUserByEmail(String email);
}
