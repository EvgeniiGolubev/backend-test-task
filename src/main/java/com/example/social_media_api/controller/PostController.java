package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "Posts", description = "API for managing posts")
@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;
    private final UserService userService;

    @Autowired
    public PostController(PostService postService, UserService userService) {
        this.postService = postService;
        this.userService = userService;
    }


    @Operation(summary = "Get all posts", description = "Retrieve a list of all posts")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Posts received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = PostDto.class)))
            )
    })
    @GetMapping
    public ResponseEntity<?> findAllPosts() {
        List<PostDto> posts = postService.findAllPosts();
        return new ResponseEntity<>(posts, HttpStatus.OK);
    }

    @Operation(summary = "Get post by ID", description = "Retrieve a post by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Post received successfully",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid post ID",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Post not found\" }") })
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> findPostById(
            @Parameter(description = "post ID")
            @PathVariable("id") Long id
    ) {
        return new ResponseEntity<>(postService.findPostById(id), HttpStatus.OK);
    }

    @Operation(summary = "Create a new post", description = "Create a new post with the provided details")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Post created successfully",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Form validation error",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Title can not be empty\" }") })
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal server error. Image processing error.",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"An error occurred while saving file\" }") })
            )
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createPost(
            @Parameter(schema = @Schema(implementation = PostDto.class))
            @Valid @ModelAttribute PostDto post,

            @Parameter(
                    description = "Image file in supported formats (JPEG, JPG, PNG). Optional parameter.",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart(name = "image", required = false) MultipartFile image,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User author = userService.getUserFromUserDetails(authenticatedUser);

        PostDto createdPost = postService.createPost(post, image, author);
        return new ResponseEntity<>(createdPost, HttpStatus.OK);
    }

    @Operation(summary = "Update a post", description = "Update an existing post with the provided details")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Post updated successfully",
                    content = @Content(schema = @Schema(implementation = PostDto.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid post ID or form validation error",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Content can not be empty\" }") })
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access denied. Only the author or admin can modify or delete the post",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Access denied. Only the author can modify or delete the post\" }") })
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal server error. Image processing error.",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"An error occurred while saving file\" }") })
            )
    })
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(
            @Parameter(description = "post ID")
            @PathVariable("id") Long id,

            @Parameter(schema = @Schema(implementation = PostDto.class))
            @Valid @ModelAttribute PostDto post,

            @Parameter(
                    description = "Image file in supported formats (JPEG, JPG, PNG). Optional parameter.",
                    content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
            )
            @RequestPart(name = "image", required = false) MultipartFile image,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User author = userService.getUserFromUserDetails(authenticatedUser);
        User actualAuthor = postService.getAuthorFromPostByPostId(id);

        checkAccess(actualAuthor, author);

        PostDto updatedPost = postService.updatePost(id, post, image);
        return new ResponseEntity<>(updatedPost, HttpStatus.OK);
    }

    @Operation(summary = "Delete a post", description = "Delete a post by its ID")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Post deleted successfully",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Post deleted successfully\" }") })
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid post ID",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Post not found\" }") })
            ),
            @ApiResponse(
                    responseCode = "403", description = "Access denied. Only the author or admin can modify or delete the post",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"Access denied. Only the author can modify or delete the post\" }") })
            ),
            @ApiResponse(
                    responseCode = "500", description = "Internal server error. Image processing error.",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"An error occurred while deleting file\" }") })
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(
            @Parameter(description = "post ID")
            @PathVariable("id") Long id,

            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl authenticatedUser
    ) {
        User author = userService.getUserFromUserDetails(authenticatedUser);
        User actualAuthor = postService.getAuthorFromPostByPostId(id);

        checkAccess(actualAuthor, author);

        postService.deletePost(id);
        return ResponseEntity.ok("Post deleted successfully");
    }

    private void checkAccess(User actualAuthor, User author) throws AccessDeniedException {
        if (!author.equals(actualAuthor) && !author.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Access denied. Only the author can modify or delete the post");
        }
    }
}
