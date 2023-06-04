package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;

import java.util.List;

public interface ProfileService {
    UserDto getUserDto(UserDetailsImpl user);
    List<UserDto> getUserSubscriptions(UserDetailsImpl user);
    List<UserDto> getUserSubscribers(UserDetailsImpl user);
    List<UserDto> getUserFriends(UserDetailsImpl user);
    void changeSubscriptionStatus(UserDetailsImpl user, Long id, Boolean status)
            throws UserNotFoundException, AccessDeniedException;
    void changeSubscription(Long id, UserDetailsImpl user, Boolean isSubscribe)
            throws UserNotFoundException, AccessDeniedException;
}
