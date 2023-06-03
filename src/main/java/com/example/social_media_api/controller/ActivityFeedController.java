package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.ArticleDto;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ArticleService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity-feed")
public class ActivityFeedController {
    private final ArticleService articleService;

    public ActivityFeedController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseEntity<?> getActivityFeed(@AuthenticationPrincipal UserDetailsImpl user,
                                             @RequestParam("sortType") String sortType,
                                             @RequestParam("page") int page,
                                             @RequestParam("pageSize") int pageSize) {
        try {
            Page<ArticleDto> articles = articleService.getArticlesBySubscriber(user, sortType, page, pageSize);
            return new ResponseEntity<>(articles, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }
}
