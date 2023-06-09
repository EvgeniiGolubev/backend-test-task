package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.FileManagerException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface PostService {
    List<PostDto> findAllPosts();
    PostDto findPostById(Long id) throws PostNotFoundException;
    PostDto createPost(PostDto post, MultipartFile image, UserDetailsImpl author)
            throws FileManagerException, IllegalArgumentException;
    PostDto updatePost(Long id, PostDto post, MultipartFile image, UserDetailsImpl author)
            throws AccessDeniedException, PostNotFoundException, FileManagerException, IllegalArgumentException;

    void deletePost(Long id, UserDetailsImpl author) throws AccessDeniedException, FileManagerException;

    Page<PostDto> getPostsBySubscriber(UserDetailsImpl user, String sortType, int page, int pageSize)
            throws IllegalArgumentException;
}
