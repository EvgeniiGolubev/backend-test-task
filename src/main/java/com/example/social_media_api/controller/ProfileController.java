package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public UserDto getUserProfile(@AuthenticationPrincipal UserDetailsImpl user) {
        return profileService.getUserDto(user);
    }

    @GetMapping("/subscriptions")
    public List<UserDto> getUserSubscriptions(@AuthenticationPrincipal UserDetailsImpl user) {
        return profileService.getUserSubscriptions(user);
    }

    @GetMapping("/subscribers")
    public List<UserDto> getUserSubscribers(@AuthenticationPrincipal UserDetailsImpl user) {
        return profileService.getUserSubscribers(user);
    }

    @GetMapping("/friends")
    public List<UserDto> getUserFriends(@AuthenticationPrincipal UserDetailsImpl user) {
        return profileService.getUserFriends(user);
    }

    @PostMapping("/change-subscription/{channelId}")
    public ResponseEntity<?> changeSubscription(@AuthenticationPrincipal UserDetailsImpl subscriber,
                                                @PathVariable("channelId") Long channelId,
                                                @RequestParam("subscribe") Boolean isSubscribe) {
        try {
            UserDto userDto = profileService.changeSubscription(channelId, subscriber, isSubscribe);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (UserNotFoundException | AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/change-status/{subscriberId}")
    public ResponseEntity<?> changeSubscriptionStatus(@AuthenticationPrincipal UserDetailsImpl chanel,
                                                            @PathVariable("subscriberId") Long subscriberId,
                                                            @RequestParam("status") Boolean status) {
        try {
            UserDto userDto = profileService.changeSubscriptionStatus(chanel, subscriberId, status);
            return new ResponseEntity<>(userDto, HttpStatus.OK);
        } catch (UserNotFoundException | AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
