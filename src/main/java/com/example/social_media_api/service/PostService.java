package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.FileManagerException;
import com.example.social_media_api.exception.PostNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {
    List<PostDto> findAllPosts();
    PostDto findPostById(Long id) throws PostNotFoundException;
    PostDto createPost(PostDto post, MultipartFile image, User author) throws FileManagerException;
    PostDto updatePost(Long id, PostDto post, MultipartFile image)
            throws PostNotFoundException, FileManagerException;

    void deletePost(Long id) throws PostNotFoundException, FileManagerException;

    Page<PostDto> getPostsBySubscriber(User user, String sortType, int page, int pageSize)
            throws IllegalArgumentException;

    User getAuthorFromPostByPostId(Long id) throws PostNotFoundException;
}
