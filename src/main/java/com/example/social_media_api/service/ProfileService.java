package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;

import java.util.List;
import java.util.Set;

public interface ProfileService {
    UserDto getUserDto(User user);
    List<UserDto> getUserSubscriptions(User user);
    List<UserDto> getUserSubscribers(User user);
    Set<UserDto> getUserFriends(User user);
    void changeSubscription(User channel, User subscriber, Boolean subscriptionStatus);
    void changeSubscriberStatus(User subscriber, User channel, Boolean subscriberStatus);
}
