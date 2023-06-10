package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.Post;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.FileManagerException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.repository.PostRepository;
import com.example.social_media_api.utils.FileManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;
    private final FileManagerUtil fileManagerUtil;


    @Autowired
    public PostServiceImpl(PostRepository postRepository, FileManagerUtil fileManagerUtil) {
        this.postRepository = postRepository;
        this.fileManagerUtil = fileManagerUtil;
    }

    @Override
    public List<PostDto> findAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PostDto> getPostsBySubscriber(User user, String sortType, int page, int pageSize)
            throws IllegalArgumentException {

        Sort sort = validPaginationAndGetSort(sortType, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Post> resultPage = postRepository.findPostsBySubscribedUsersSortedByDate(user.getId(), pageable);

        return resultPage.map(PostDto::new);
    }

    @Override
    public PostDto findPostById(Long id) throws PostNotFoundException {
        Post postFromDb = checkPostPresentAndGet(id);

        return new PostDto(postFromDb);
    }

    @Override
    public PostDto createPost(PostDto post, MultipartFile image, User author) throws FileManagerException {

        String imageLink = fileManagerUtil.saveFileAndGetLink(image);

        Post newPost = new Post(
                post.getTitle(),
                post.getContent(),
                imageLink,
                author,
                LocalDateTime.now()
        );

        return new PostDto(postRepository.save(newPost));
    }

    @Override
    public PostDto updatePost(Long id, PostDto postDto, MultipartFile image)
            throws PostNotFoundException, FileManagerException {

        Post post = checkPostPresentAndGet(id);

        String imageLink = fileManagerUtil.saveFileAndGetLink(image);
        if (imageLink == null) {
            imageLink = post.getImageLink();
        } else {
            fileManagerUtil.deleteFile(post.getImageLink());
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setImageLink(imageLink);

        return new PostDto(postRepository.save(post));
    }

    @Override
    public void deletePost(Long id) throws PostNotFoundException, FileManagerException {
        Post post = checkPostPresentAndGet(id);

        fileManagerUtil.deleteFile(post.getImageLink());

        postRepository.delete(post);
    }

    @Override
    public User getAuthorFromPostByPostId(Long id) throws PostNotFoundException {
        Post post = checkPostPresentAndGet(id);
        return post.getAuthor();
    }

    private Post checkPostPresentAndGet(Long id) throws PostNotFoundException {
        Post postFromDb = postRepository.findById(id).orElse(null);

        if (postFromDb == null) {
            throw new PostNotFoundException("Post not found");
        }

        return postFromDb;
    }

    private Sort validPaginationAndGetSort(String sortType, int page, int pageSize) {
        if (sortType == null) {
            throw new IllegalArgumentException("Sort type cannot be null");
        }

        if (page < 0) {
            throw new IllegalArgumentException("Page number must be non-negative");
        }

        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive");
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid sortType value! Must be 'DESC' or 'ASC");
        }

        return Sort.by(direction, "createDate");
    }
}
