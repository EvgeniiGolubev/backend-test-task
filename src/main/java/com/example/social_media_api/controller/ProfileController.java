package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ProfileService;
import com.example.social_media_api.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Tag(name = "Profile", description = "API for managing user profile (manage subscriptions and subscribers user's)")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {
    private final ProfileService profileService;
    private final UserService userService;

    @Autowired
    public ProfileController(ProfileService profileService, UserService userService) {
        this.profileService = profileService;
        this.userService = userService;
    }

    @Operation(summary = "Get user profile", description = "Get the profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Profile received successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
    })
    @GetMapping
    public ResponseEntity<?> getUserProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User user = userService.getUserFromUserDetails(authenticatedUser);

        UserDto userDto = profileService.getUserDto(user);
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }

    @Operation(summary = "Get user subscriptions", description = "Get the subscriptions list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User subscriptions received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/subscriptions")
    public ResponseEntity<?> getUserSubscriptions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User user = userService.getUserFromUserDetails(authenticatedUser);

        List<UserDto> subscriptions = profileService.getUserSubscriptions(user);
        return new ResponseEntity<>(subscriptions, HttpStatus.OK);
    }

    @Operation(summary = "Get user subscribers", description = "Get the subscribers list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User subscribers received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/subscribers")
    public ResponseEntity<?> getUserSubscribers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User user = userService.getUserFromUserDetails(authenticatedUser);

        List<UserDto> subscribers = profileService.getUserSubscribers(user);
        return new ResponseEntity<>(subscribers, HttpStatus.OK);
    }

    @Operation(summary = "Get user friends", description = "Get the friends list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User friends received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/friends")
    public ResponseEntity<?> getUserFriends(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User user = userService.getUserFromUserDetails(authenticatedUser);

        Set<UserDto> friends = profileService.getUserFriends(user);
        return new ResponseEntity<>(friends, HttpStatus.OK);
    }

    @Operation(
            summary = "Change channel subscription status",
            description = "Change the subscription status of an authenticated user to a specific channel of another user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Channel subscription status changed successfully",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Subscription changed successfully\" }") })
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid channel ID or attempt to subscribe to yourself",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"You can not follow yourself\" }") })
            )
    })
    @PostMapping("/change-subscription/{channelId}")
    public ResponseEntity<?> changeSubscription(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser,

            @Parameter(description = "channel ID")
            @PathVariable("channelId") Long channelId,

            @Parameter(description = "Subscribe or unsubscribe from a channel (true if subscribe, false if unsubscribe)")
            @RequestParam("subscribe") Boolean subscriptionStatus
    ) {
        User subscriber = userService.getUserFromUserDetails(authenticatedUser);
        User channel = userService.findUserById(channelId);

        if (channel.equals(subscriber)) {
            throw new AccessDeniedException("You can not follow yourself");
        }

        profileService.changeSubscription(channel, subscriber, subscriptionStatus);
        return new ResponseEntity<>("Subscription changed successfully", HttpStatus.OK);
    }

    @Operation(
            summary = "Change a subscriber's subscription status",
            description = "Change the subscription state of an authenticated user's channel subscriber"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Subscription status of a subscriber to an authenticated user has been successfully changed",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Status subscription changed successfully\" }") })
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid subscriber ID or attempt to change the subscription status to itself",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"You can not follow yourself\" }") })
            )
    })
    @PostMapping("/change-status/{subscriberId}")
    public ResponseEntity<?> changeSubscriberStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser,

            @Parameter(description = "subscriber ID")
            @PathVariable("subscriberId") Long subscriberId,

            @Parameter(description = "Accept or reject a subscriber (true if accept, false if reject)")
            @RequestParam("status") Boolean subscriberStatus
    ) {
        User channel = userService.getUserFromUserDetails(authenticatedUser);
        User subscriber = userService.findUserById(subscriberId);

        if (channel.equals(subscriber)) {
            throw new AccessDeniedException("You can not follow yourself");
        }

        profileService.changeSubscriberStatus(subscriber, channel, subscriberStatus);
        return new ResponseEntity<>("Subscriber status changed successfully", HttpStatus.OK);
    }
}
