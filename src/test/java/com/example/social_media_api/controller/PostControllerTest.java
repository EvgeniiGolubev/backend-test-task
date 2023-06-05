package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PostControllerTest {

    @InjectMocks
    private PostController postController;

    @Mock
    private PostService postService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindAllPosts() {
        List<PostDto> posts = new ArrayList<>();
        posts.add(new PostDto());
        posts.add(new PostDto());

        when(postService.findAllPosts()).thenReturn(posts);

        List<PostDto> result = postController.findAllPosts();

        assertEquals(posts.size(), result.size());
        assertEquals(posts, result);

        verify(postService, times(1)).findAllPosts();

    }

    @Test
    public void testFindPostById() throws PostNotFoundException {
        Long postId = 1L;
        PostDto post = new PostDto();

        when(postService.findPostById(postId)).thenReturn(post);

        ResponseEntity<?> responseEntity = postController.findPostById(postId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(post, responseEntity.getBody());

        verify(postService, times(1)).findPostById(postId);
    }

    @Test
    public void testCreatePost() throws IOException {
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image".getBytes());
        PostDto postDto = new PostDto();

        when(postService.createPost(postDto, image, authenticatedUser)).thenReturn(postDto);

        ResponseEntity<?> responseEntity = postController.createPost(postDto, image, authenticatedUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(postDto, responseEntity.getBody());

        verify(postService, times(1)).createPost(postDto, image, authenticatedUser);
    }

    @Test
    public void testUpdatePost() throws IOException, PostNotFoundException, AccessDeniedException {
        Long postId = 1L;
        MockMultipartFile image = new MockMultipartFile("image", "image.jpg", "image/jpeg", "test image".getBytes());
        PostDto postDto = new PostDto();

        when(postService.updatePost(postId, postDto, image, authenticatedUser)).thenReturn(postDto);

        ResponseEntity<?> responseEntity = postController.updatePost(postId, postDto, image, authenticatedUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(postDto, responseEntity.getBody());

        verify(postService, times(1)).updatePost(postId, postDto, image, authenticatedUser);

    }

    @Test
    public void testDeletePost() throws PostNotFoundException, AccessDeniedException, IOException {
        Long postId = 1L;

        ResponseEntity<?> responseEntity = postController.deletePost(postId, authenticatedUser);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Post delete successfully", responseEntity.getBody());

        verify(postService, times(1)).deletePost(postId, authenticatedUser);

    }
}