package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Activity feed", description = "API for getting a list of posts from users to which the authenticated user is subscribed")
@RestController
@RequestMapping("/api/activity-feed")
public class ActivityFeedController {
    private final PostService postService;

    public ActivityFeedController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Get activity feed", description = "Get all user posts that a authenticated user is following")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Activity feed received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid request params",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Page number must be non-negative\" }") })
            )
    })
    @GetMapping
    public ResponseEntity<?> getActivityFeed(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl user,

            @Parameter(description = "Type of sorting posts by date. Valid values: DESC or ASC.")
            @RequestParam("sortType") String sortType,

            @Parameter(description = "Current page. The minimum value is 0.")
            @RequestParam("page") int page,

            @Parameter(description = "Current page count. The minimum value is 1.")
            @RequestParam("pageSize") int pageSize
    ) {
        try {
            Page<PostDto> posts = postService.getPostsBySubscriber(user, sortType, page, pageSize);
            return new ResponseEntity<>(posts, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
