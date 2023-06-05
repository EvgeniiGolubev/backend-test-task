package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ProfileService;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfileControllerTest {
    @InjectMocks
    private ProfileController profileController;

    @Mock
    private ProfileService profileService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserProfile() {
        UserDto expectedUserDto = new UserDto();

        when(profileService.getUserDto(authenticatedUser)).thenReturn(expectedUserDto);

        UserDto result = profileController.getUserProfile(authenticatedUser);

        assertEquals(expectedUserDto, result);
        verify(profileService, times(1)).getUserDto(authenticatedUser);
    }

    @Test
    public void testGetUserSubscriptions() {
        List<UserDto> expectedSubscriptions = new ArrayList<>();

        when(profileService.getUserSubscriptions(authenticatedUser)).thenReturn(expectedSubscriptions);

        List<UserDto> result = profileController.getUserSubscriptions(authenticatedUser);

        assertEquals(expectedSubscriptions, result);
        verify(profileService, times(1)).getUserSubscriptions(authenticatedUser);
    }

    @Test
    public void testGetUserSubscribers() {
        List<UserDto> expectedSubscribers = new ArrayList<>();

        when(profileService.getUserSubscribers(authenticatedUser)).thenReturn(expectedSubscribers);

        List<UserDto> result = profileController.getUserSubscribers(authenticatedUser);

        assertEquals(expectedSubscribers, result);
        verify(profileService, times(1)).getUserSubscribers(authenticatedUser);
    }

    @Test
    public void testGetUserFriends() {
        Set<UserDto> expectedFriends = new HashSet<>();

        when(profileService.getUserFriends(authenticatedUser)).thenReturn(expectedFriends);

        Set<UserDto> result = profileController.getUserFriends(authenticatedUser);

        assertEquals(expectedFriends, result);
        verify(profileService, times(1)).getUserFriends(authenticatedUser);
    }

    @Test
    public void testChangeSubscription() throws UserNotFoundException, AccessDeniedException {
        Long channelId = 1L;
        Boolean isSubscribe = true;

        ResponseEntity<?> expectedResponse = new ResponseEntity<>("Channel subscription status changed successfully", HttpStatus.OK);

        doNothing().when(profileService).changeSubscription(channelId, authenticatedUser, isSubscribe);

        ResponseEntity<?> result = profileController.changeSubscription(authenticatedUser, channelId, isSubscribe);

        assertEquals(expectedResponse, result);
        verify(profileService, times(1)).changeSubscription(channelId, authenticatedUser, isSubscribe);
    }

    @Test
    public void testChangeSubscriptionStatus() throws UserNotFoundException, AccessDeniedException {
        Long subscriberId = 1L;
        Boolean status = true;

        ResponseEntity<?> expectedResponse = new ResponseEntity<>("Status subscription changed successfully", HttpStatus.OK);

        doNothing().when(profileService).changeSubscriptionStatus(authenticatedUser, subscriberId, status);

        ResponseEntity<?> result = profileController.changeSubscriptionStatus(authenticatedUser, subscriberId, status);

        assertEquals(expectedResponse, result);
        verify(profileService, times(1)).changeSubscriptionStatus(authenticatedUser, subscriberId, status);
    }
}