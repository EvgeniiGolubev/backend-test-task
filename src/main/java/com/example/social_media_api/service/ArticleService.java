package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.ArticleDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.ArticleNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ArticleService {
    List<ArticleDto> findAllArticles();
    ArticleDto findArticlesById(Long id) throws ArticleNotFoundException;
    ArticleDto createArticle(ArticleDto article, MultipartFile image, UserDetailsImpl author)
            throws IOException, IllegalArgumentException;
    ArticleDto updateArticle(Long id, ArticleDto article, MultipartFile image, UserDetailsImpl author)
            throws AccessDeniedException, ArticleNotFoundException, IOException, IllegalArgumentException;

    void deleteArticle(Long id, UserDetailsImpl author) throws AccessDeniedException, IOException;

    Page<ArticleDto> getArticlesBySubscriber(UserDetailsImpl user, String sortType, int page, int pageSize)
            throws IllegalArgumentException;
}
