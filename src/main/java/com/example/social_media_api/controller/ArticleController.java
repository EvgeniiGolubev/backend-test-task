package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.ArticleDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.ArticleNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;

    @Autowired
    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public List<ArticleDto> findAllArticles() {
        return articleService.findAllArticles();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> findArticleById(@PathVariable("id") Long id) {
        try {
            return new ResponseEntity<>(articleService.findArticlesById(id), HttpStatus.OK);
        } catch (ArticleNotFoundException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    public ResponseEntity<?> createArticle(@ModelAttribute ArticleDto articleDto,
                                           @RequestParam(name = "image", required = false) MultipartFile image,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            ArticleDto createdArticle = articleService.createArticle(articleDto, image, author);

            return new ResponseEntity<>(createdArticle, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to save file"), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("{id}")
    public ResponseEntity<?> updateArticle(@PathVariable("id") Long id,
                                           @ModelAttribute ArticleDto article,
                                           @RequestParam(name = "image", required = false) MultipartFile image,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            ArticleDto updatedArticle = articleService.updateArticle(id, article, image, author);

            return new ResponseEntity<>(updatedArticle, HttpStatus.OK);
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (ArticleNotFoundException | IllegalArgumentException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to save file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteArticle(@PathVariable("id") Long id,
                                           @AuthenticationPrincipal UserDetailsImpl author) {
        try {
            articleService.deleteArticle(id, author);
            return ResponseEntity.ok("Delete success");
        } catch (AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.FORBIDDEN);
        } catch (ArticleNotFoundException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        } catch (IOException e) {
            return new ResponseEntity<>(new ResponseMessage("Failed to remove file"), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
