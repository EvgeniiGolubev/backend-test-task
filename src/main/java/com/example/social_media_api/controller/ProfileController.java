package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ProfileService;
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

    @Autowired
    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Operation(summary = "Get user profile", description = "Get the profile of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Profile received successfully",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            )
    })
    @GetMapping
    public UserDto getUserProfile(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return profileService.getUserDto(user);
    }

    @Operation(summary = "Get user subscriptions", description = "Get the subscriptions list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User subscriptions received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/subscriptions")
    public List<UserDto> getUserSubscriptions(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return profileService.getUserSubscriptions(user);
    }

    @Operation(summary = "Get user subscribers", description = "Get the subscribers list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User subscribers received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/subscribers")
    public List<UserDto> getUserSubscribers(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return profileService.getUserSubscribers(user);
    }

    @Operation(summary = "Get user friends", description = "Get the friends list of the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "User friends received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserDto.class)))
            )
    })
    @GetMapping("/friends")
    public Set<UserDto> getUserFriends(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user
    ) {
        return profileService.getUserFriends(user);
    }

    @Operation(
            summary = "Change channel subscription status",
            description = "Change the subscription status of an authenticated user to a specific channel of another user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Channel subscription status changed successfully",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Channel subscription status changed successfully\" }") })
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
            @AuthenticationPrincipal UserDetailsImpl subscriber,

            @Parameter(description = "channel ID")
            @PathVariable("channelId") Long channelId,

            @Parameter(description = "Subscribe or unsubscribe from a channel (true if subscribe, false if unsubscribe)")
            @RequestParam("subscribe") Boolean isSubscribe
    ) {
        profileService.changeSubscription(channelId, subscriber, isSubscribe);
        return new ResponseEntity<>("Channel subscription status changed successfully", HttpStatus.OK);
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
    public ResponseEntity<?> changeSubscriptionStatus(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl chanel,

            @Parameter(description = "subscriber ID")
            @PathVariable("subscriberId") Long subscriberId,

            @Parameter(description = "Accept or reject a subscriber (true if accept, false if reject)")
            @RequestParam("status") Boolean status
    ) {
        profileService.changeSubscriptionStatus(chanel, subscriberId, status);
        return new ResponseEntity<>("Status subscription changed successfully", HttpStatus.OK);
    }
}
