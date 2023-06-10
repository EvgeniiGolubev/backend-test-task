package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ProfileService;
import com.example.social_media_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

class ProfileControllerTest {
    @InjectMocks
    private ProfileController profileController;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getUserProfile() {
        User user = new User();
        UserDto expectedUserDto = new UserDto();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(profileService.getUserDto(user)).thenReturn(expectedUserDto);

        ResponseEntity<?> result = profileController.getUserProfile(authenticatedUser);

        assertEquals(expectedUserDto, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(profileService, times(1)).getUserDto(user);
    }

    @Test
    public void getUserSubscriptions() {
        List<UserDto> expectedSubscriptions = new ArrayList<>();
        User user = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(profileService.getUserSubscriptions(user)).thenReturn(expectedSubscriptions);

        ResponseEntity<?> result = profileController.getUserSubscriptions(authenticatedUser);

        assertEquals(expectedSubscriptions, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(profileService, times(1)).getUserSubscriptions(user);
    }

    @Test
    public void getUserSubscribers() {
        List<UserDto> expectedSubscribers = new ArrayList<>();
        User user = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(profileService.getUserSubscribers(user)).thenReturn(expectedSubscribers);

        ResponseEntity<?> result = profileController.getUserSubscribers(authenticatedUser);

        assertEquals(expectedSubscribers, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(profileService, times(1)).getUserSubscribers(user);
    }

    @Test
    public void getUserFriends() {
        Set<UserDto> expectedFriends = new HashSet<>();
        User user = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(profileService.getUserFriends(user)).thenReturn(expectedFriends);

        ResponseEntity<?> result = profileController.getUserFriends(authenticatedUser);

        assertEquals(expectedFriends, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(profileService, times(1)).getUserFriends(user);
    }

    @Test
    public void changeSubscription() throws UserNotFoundException, AccessDeniedException {
        Long channelId = 1L;
        Boolean subscriptionStatus = true;

        User subscriber = new User();
        subscriber.setId(2L);
        User channel = new User();
        channel.setId(channelId);

        ResponseEntity<?> expectedResponse = new ResponseEntity<>("Subscription changed successfully", HttpStatus.OK);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(subscriber);
        when(userService.findUserById(channelId)).thenReturn(channel);

        ResponseEntity<?> result = profileController.changeSubscription(authenticatedUser, channelId, subscriptionStatus);

        assertEquals(expectedResponse, result);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(channelId);
        verify(profileService, times(1)).changeSubscription(channel, subscriber, subscriptionStatus);
    }

    @Test
    public void changeSubscriberStatus() throws UserNotFoundException, AccessDeniedException {
        Long subscriberId = 1L;
        Boolean subscriberStatus = true;

        User subscriber = new User();
        subscriber.setId(subscriberId);
        User channel = new User();
        channel.setId(2L);

        ResponseEntity<?> expectedResponse = new ResponseEntity<>("Subscriber status changed successfully", HttpStatus.OK);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(channel);
        when(userService.findUserById(subscriberId)).thenReturn(subscriber);

        ResponseEntity<?> result = profileController.changeSubscriberStatus(authenticatedUser, subscriberId, subscriberStatus);

        assertEquals(expectedResponse, result);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(subscriberId);
        verify(profileService, times(1)).changeSubscriberStatus(subscriber, channel, subscriberStatus);
    }
}