package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class PostRepositoryTest {

    @Mock
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindPostsBySubscribedUsersSortedByDate() {
        Long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        List<Post> posts = new ArrayList<>();
        Page<Post> expectedPage = new PageImpl<>(posts);

        when(postRepository.findPostsBySubscribedUsersSortedByDate(userId, pageable)).thenReturn(expectedPage);

        Page<Post> result = postRepository.findPostsBySubscribedUsersSortedByDate(userId, pageable);

        assertEquals(expectedPage, result);
    }

    @Test
    public void testSave() {
        Post post = new Post();
        when(postRepository.save(post)).thenReturn(post);

        Post result = postRepository.save(post);

        assertEquals(post, result);
    }

    @Test
    public void testDelete() {
        Post post = new Post();

        postRepository.delete(post);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    public void testFindById() {
        Long id = 1L;
        Post post = new Post();
        when(postRepository.findById(id)).thenReturn(Optional.of(post));

        Post result = postRepository.findById(id).orElseGet(null);

        assertEquals(post, result);
    }
}