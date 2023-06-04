package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PostServiceImplTest {
    @InjectMocks
    PostServiceImpl postService;

    @Test
    void findAllPosts() {
    }

    @Test
    void getPostsBySubscriber() {
    }

    @Test
    void findPostById() {
    }

    @Test
    void createPost() {
    }

    @Test
    void updatePost() throws IOException {
        PostDto postDto = postService.updatePost(null, null, null, null);

        assertNull(postDto);
    }

    @Test
    void updatePostIfTitleIsNullOrBlank() throws IOException {
        PostDto postDto = new PostDto();
        postDto.setTitle("title");
        postDto.setContent("content");

        postService.updatePost(null, postDto, null, null);

        assertNull(postDto.getTitle());
    }

    @Test
    void deletePost() {
    }
}