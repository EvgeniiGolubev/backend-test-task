package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.NewUserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.UserAlreadyExistsException;
import com.example.social_media_api.exception.UserNotFoundException;

public interface UserService {
    User saveUser(NewUserDto newUserDto);

    User findUserByEmail(String email);

    User findUserByName(String name);

    User findUserById(Long id) throws UserNotFoundException;

    User updateUser(User user);
}
