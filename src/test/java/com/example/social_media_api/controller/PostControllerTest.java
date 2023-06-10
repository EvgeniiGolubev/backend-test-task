package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
import com.example.social_media_api.service.UserService;
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
import static org.mockito.Mockito.verify;

class PostControllerTest {

    @InjectMocks
    private PostController postController;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void findAllPosts() {
        List<PostDto> posts = new ArrayList<>();
        PostDto post = new PostDto();
        post.setId(1L);
        posts.add(post);

        when(postService.findAllPosts()).thenReturn(posts);

        ResponseEntity<?> result = postController.findAllPosts();

        assertEquals(posts, result.getBody());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(postService, times(1)).findAllPosts();

    }

    @Test
    public void findPostById() {
        Long postId = 1L;
        PostDto post = new PostDto();

        when(postService.findPostById(postId)).thenReturn(post);

        ResponseEntity<?> responseEntity = postController.findPostById(postId);

        assertEquals(post, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(postService, times(1)).findPostById(postId);
    }

    @Test
    public void createPost() {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "image.jpg",
                "image/jpeg",
                "test image".getBytes()
        );
        PostDto postDto = new PostDto();
        User user = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(postService.createPost(postDto, image, user)).thenReturn(postDto);

        ResponseEntity<?> responseEntity = postController.createPost(postDto, image, authenticatedUser);

        assertEquals(postDto, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postService, times(1)).createPost(postDto, image, user);
    }

    @Test
    public void updatePost() throws IOException, PostNotFoundException, AccessDeniedException {
        Long postId = 1L;
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "image.jpg",
                "image/jpeg",
                "test image".getBytes()
        );
        PostDto postDto = new PostDto();
        User sameAuthor = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sameAuthor);
        when(postService.getAuthorFromPostByPostId(postId)).thenReturn(sameAuthor);
        when(postService.updatePost(postId, postDto, image)).thenReturn(postDto);

        ResponseEntity<?> responseEntity = postController.updatePost(postId, postDto, image, authenticatedUser);

        assertEquals(postDto, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postService, times(1)).getAuthorFromPostByPostId(postId);
        verify(postService, times(1)).updatePost(postId, postDto, image);
    }

    @Test
    public void deletePost() throws PostNotFoundException, AccessDeniedException, IOException {
        Long postId = 1L;
        User author = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(author);
        when(postService.getAuthorFromPostByPostId(postId)).thenReturn(author);

        ResponseEntity<?> responseEntity = postController.deletePost(postId, authenticatedUser);

        assertEquals("Post deleted successfully", responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postService, times(1)).deletePost(postId);

    }
}