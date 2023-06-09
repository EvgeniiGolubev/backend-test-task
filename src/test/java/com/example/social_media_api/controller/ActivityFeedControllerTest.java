package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.PostService;
import com.example.social_media_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ActivityFeedControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @InjectMocks
    private ActivityFeedController activityFeedController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetActivityFeed() {
        UserDetailsImpl authenticatedUser = new UserDetailsImpl();
        User user = new User();

        String sortType = "date";
        int page = 1;
        int pageSize = 10;
        Page<PostDto> posts = new PageImpl<>(Collections.singletonList(new PostDto()), PageRequest.of(page, pageSize), 1);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(user);
        when(postService.getPostsBySubscriber(user, sortType, page, pageSize)).thenReturn(posts);

        ResponseEntity<?> responseEntity = activityFeedController.getActivityFeed(authenticatedUser, sortType, page, pageSize);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(posts, responseEntity.getBody());
        verify(postService, times(1)).getPostsBySubscriber(user, sortType, page, pageSize);
    }
}