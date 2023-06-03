package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostDto> findAllPosts() {
        return postService.findAllPosts();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> findPostById(@PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(postService.findPostById(id), HttpStatus.OK);
        } catch (PostNotFoundException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@ModelAttribute PostDto post,
                                           @RequestParam(name = "image", required = false) MultipartFile image,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            PostDto createdPost = postService.createPost(post, image, author);

            return new ResponseEntity<>(createdPost, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to save file"), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updatePost(@PathVariable("id") Long id,
                                           @ModelAttribute PostDto post,
                                           @RequestParam(name = "image", required = false) MultipartFile image,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            PostDto updatedPost = postService.updatePost(id, post, image, author);

            return new ResponseEntity<>(updatedPost, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (PostNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to save file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deletePost(@PathVariable("id") Long id,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            postService.deletePost(id, author);
            return ResponseEntity.ok("Post delete success");
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (PostNotFoundException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to remove file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
