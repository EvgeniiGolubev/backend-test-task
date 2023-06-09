package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.entity.Post;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.FileManagerException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.repository.PostRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostServiceImpl implements PostService {

    @Value("${upload.path}")
    private String uploadPath;
    private final PostRepository postRepository;
    private final UserService userService;

    @Autowired
    public PostServiceImpl(PostRepository postRepository, UserService userService) {
        this.postRepository = postRepository;
        this.userService = userService;
    }

    @Override
    public List<PostDto> findAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PostDto> getPostsBySubscriber(UserDetailsImpl user, String sortType, int page, int pageSize)
            throws IllegalArgumentException {
        Sort sort = validPaginationAndGetSort(sortType, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        User userFromDb = userService.getUserFromUserDetails(user);

        Page<Post> resultPage = postRepository.findPostsBySubscribedUsersSortedByDate(userFromDb.getId(), pageable);

        return resultPage.map(PostDto::new);
    }

    @Override
    public PostDto findPostById(Long id) throws PostNotFoundException {
        Post postFromDb = checkPostPresentAndGet(id);

        return new PostDto(postFromDb);
    }

    @Override
    public PostDto createPost(PostDto post, MultipartFile image, UserDetailsImpl author)
            throws FileManagerException, IllegalArgumentException {

        validPostFields(post);

        String imageLink = saveFileAndGetLink(image);

        User userAuthor = userService.getUserFromUserDetails(author);

        Post newPost = new Post(
                post.getTitle(),
                post.getContent(),
                imageLink,
                userAuthor,
                LocalDateTime.now()
        );

        return new PostDto(postRepository.save(newPost));
    }

    @Override
    public PostDto updatePost(Long id, PostDto post, MultipartFile image, UserDetailsImpl author)
            throws AccessDeniedException, PostNotFoundException, FileManagerException, IllegalArgumentException {

        validPostFields(post);

        Post postFromDb = checkPostPresentAndGet(id);

        checkAccess(postFromDb, author);

        String imageLink = saveFileAndGetLink(image);
        if (imageLink == null) {
            imageLink = post.getImageLink();
        } else {
            deleteFile(post.getImageLink());
        }

        postFromDb.setTitle(post.getTitle());
        postFromDb.setContent(post.getContent());
        postFromDb.setImageLink(imageLink);

        return new PostDto(postRepository.save(postFromDb));
    }

    @Override
    public void deletePost(Long id, UserDetailsImpl author)
            throws AccessDeniedException, PostNotFoundException, FileManagerException {

        Post postFromDb = checkPostPresentAndGet(id);

        checkAccess(postFromDb, author);

        deleteFile(postFromDb.getImageLink());

        postRepository.delete(postFromDb);
    }

    private String saveFileAndGetLink(MultipartFile image) throws FileManagerException, IllegalArgumentException {
        if (image == null || image.getOriginalFilename().isEmpty() || uploadPath == null) {
            return null;
        }

        try {
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);

            String uuidFile = UUID.randomUUID().toString();
            String originFileName = image.getOriginalFilename();
            String extension = originFileName.substring(originFileName.lastIndexOf(".")).toLowerCase();

            if (!extension.matches("\\.(jpg|jpeg|png)")) {
                throw new IllegalArgumentException("Invalid image format. Only JPG, JPEG, and PNG formats are allowed");
            }

            String resultFileName = uuidFile + extension;
            Path filePath = Paths.get(uploadPath, resultFileName);
            image.transferTo(Files.createFile(filePath));

            return resultFileName;
        } catch (IOException e) {
            throw new FileManagerException("An error occurred while saving files");
        }
    }

    private void deleteFile(String filename) throws FileManagerException {

        try {
            if (filename != null) {
                Path filePath = Paths.get(uploadPath, filename);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }
        } catch (IOException e) {
            throw new FileManagerException("An error occurred while deleting files");
        }
    }

    private Post checkPostPresentAndGet(Long id) throws PostNotFoundException {
        Post postFromDb = postRepository.findById(id).orElse(null);

        if (postFromDb == null) {
            throw new PostNotFoundException("Post not found");
        }

        return postFromDb;
    }

    private void checkAccess(Post post, UserDetailsImpl author) throws AccessDeniedException{
        User userAuthor = userService.getUserFromUserDetails(author);
        User actualAuthor = post.getAuthor();

        if (!userAuthor.equals(actualAuthor) && !actualAuthor.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Access denied. Only the author can modify or delete the post");
        }
    }

    private void validPostFields(PostDto post) throws IllegalArgumentException {
        String title = post.getTitle();
        String content = post.getContent();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title can not be empty");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content can not be empty");
        }
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
