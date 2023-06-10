package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.Post;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.repository.PostRepository;
import com.example.social_media_api.utils.FileManagerUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class PostServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FileManagerUtil fileManagerUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllPosts() {
        Post first = new Post();
        first.setId(1L);
        first.setAuthor(new User());
        Post second = new Post();
        second.setId(1L);
        second.setAuthor(new User());

        List<Post> posts = new ArrayList<>(Arrays.asList(first, second));
        List<PostDto> expected = posts.stream().map(PostDto::new).toList();

        when(postRepository.findAll()).thenReturn(posts);

        List<PostDto> result = postService.findAllPosts();

        assertEquals(expected, result);
        verify(postRepository, times(1)).findAll();
    }

    @Test
    void getPostsBySubscriberWithValidParams() {
        Post first = new Post();
        first.setId(1L);
        first.setAuthor(new User());
        Post second = new Post();
        second.setId(1L);
        second.setAuthor(new User());

        List<Post> posts = new ArrayList<>(Arrays.asList(first, second));

        String sortType = "ASC";
        int page = 0;
        int pageSize = 10;

        User user = new User();
        user.setId(1L);

        Sort sort = Sort.by(Sort.Direction.ASC, "createDate");
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<Post> postsPage = new PageImpl<>(posts, pageable, posts.size());
        Page<PostDto> expected = postsPage.map(PostDto::new);

        when(postRepository.findPostsBySubscribedUsersSortedByDate(user.getId(), pageable)).thenReturn(postsPage);

        Page<PostDto> resultPage = postService.getPostsBySubscriber(user, sortType, page, pageSize);

        assertEquals(expected, resultPage);
        verify(postRepository, times(1)).findPostsBySubscribedUsersSortedByDate(user.getId(), pageable);
    }

    @Test
    void getPostsBySubscriberWithSortTypeNullAndThrowsIllegalArgumentException() {
        String sortType = null;
        int page = 0;
        int pageSize = 10;

        User user = new User();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(user, sortType, page, pageSize)
        );

        assertEquals("Sort type cannot be null", exception.getMessage());
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidSortTypeAndThrowsIllegalArgumentException() {
        String sortType = "ERROR";
        int page = 0;
        int pageSize = 10;

        User user = new User();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(user, sortType, page, pageSize)
        );

        assertEquals("Invalid sortType value! Must be 'DESC' or 'ASC", exception.getMessage());
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidPageAndThrowsIllegalArgumentException() {
        String sortType = "ASC";
        int page = -1;
        int pageSize = 10;

        User user = new User();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(user, sortType, page, pageSize)
        );

        assertEquals("Page number must be non-negative", exception.getMessage());
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidPageSizeAndThrowsIllegalArgumentException() {
        String sortType = "ASC";
        int page = 1;
        int pageSize = -1;

        User user = new User();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(user, sortType, page, pageSize)
        );

        assertEquals("Page size must be positive", exception.getMessage());
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void findPostById() {
        Long postId = 1L;

        Post post = new Post();
        post.setId(1L);
        post.setAuthor(new User());

        PostDto expectedPostDto = new PostDto(post);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        PostDto result = postService.findPostById(postId);

        assertEquals(expectedPostDto, result);
        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    void findPostByIdWidthInvalidIdAndThrowsPostNotFoundException() {
        Long postId = -1L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThrows(
                PostNotFoundException.class,
                () -> postService.findPostById(postId),
                "Post not found"
        );

        verify(postRepository, times(1)).findById(postId);
    }

    @Test
    void createPostWithValidPostFields() throws IOException {
        Post post = new Post();
        post.setTitle("Title");
        post.setContent("Content");
        post.setAuthor(new User());

        PostDto expected = new PostDto(post);

        User author = new User();
        author.setId(1L);

        MultipartFile image = new MockMultipartFile("test.png", "test.png", "image/png",
                Files.readAllBytes(Paths.get("src/test/resources/test.png")));

        when(postRepository.save(post)).thenReturn(post);

        PostDto result = postService.createPost(expected, image, author);

        assertEquals(expected, result);
        verify(fileManagerUtil, times(1)).saveFileAndGetLink(image);
        verify(postRepository, times(1)).save(post);
    }

    @Test
    void createPostWithInvalidImageFormatAndThrowsIllegalArgumentException() throws IOException {
        PostDto postDto = new PostDto();
        postDto.setTitle("Title");
        postDto.setContent("Content");

        User author = new User();

        MultipartFile invalidImage = new MockMultipartFile("test.gif", "test.gif", "image/gif",
                Files.readAllBytes(Paths.get("src/test/resources/test.gif")));

        when(fileManagerUtil.saveFileAndGetLink(invalidImage))
                .thenThrow(new IllegalArgumentException("Invalid image format. Only JPG, JPEG, and PNG formats are allowed"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(postDto, invalidImage, author)
        );

        assertEquals("Invalid image format. Only JPG, JPEG, and PNG formats are allowed", exception.getMessage());
        verify(fileManagerUtil, times(1)).saveFileAndGetLink(invalidImage);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePostWithValidPostFields() throws IOException {
        Long postId = 1L;
        User author = new User();
        author.setId(1L);

        PostDto postDto = new PostDto();
        postDto.setId(postId);
        postDto.setTitle("Title");
        postDto.setContent("Content");
        postDto.setAuthor(new UserDto(author));
        postDto.setCreateDate(LocalDateTime.now());

        MultipartFile image = new MockMultipartFile(
                "test.png",
                "test.png",
                "image/png",
                Files.readAllBytes(Paths.get("src/test/resources/test.png")));

        Post postFromDb = new Post();
        postFromDb.setId(postId);
        postFromDb.setTitle("New title");
        postFromDb.setContent("New content");
        postFromDb.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostDto result = postService.updatePost(postId, postDto, image);

        PostDto expectedPostDto = new PostDto(postFromDb);

        assertEquals(expectedPostDto, result);
        verify(fileManagerUtil, times(1)).saveFileAndGetLink(image);
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePostWithInvalidIDAndThrowsPostNotFoundException() {
        Long postId = -1L;

        PostDto postDto = new PostDto();
        postDto.setTitle("Updated Title");
        postDto.setContent("Updated Content");
        postDto.setAuthor(new UserDto(new User()));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.updatePost(postId, postDto, null)
        );

        assertEquals("Post not found", exception.getMessage());
        verify(fileManagerUtil, never()).saveFileAndGetLink(null);
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePostWithInvalidImageFormatAndThrowsIllegalArgumentException() throws IOException {
        User author = new User();

        Long postId = 1L;
        PostDto postDto = new PostDto();
        postDto.setId(postId);
        postDto.setTitle("Title");
        postDto.setContent("Content");

        Post postFromDb = new Post();
        postFromDb.setAuthor(author);

        MultipartFile invalidImage = new MockMultipartFile("test.gif", "test.gif", "image/gif",
                Files.readAllBytes(Paths.get("src/test/resources/test.gif")));

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));
        when(fileManagerUtil.saveFileAndGetLink(invalidImage))
                .thenThrow(new IllegalArgumentException("Invalid image format. Only JPG, JPEG, and PNG formats are allowed"));


        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.updatePost(postId, postDto, invalidImage)
        );

        assertEquals("Invalid image format. Only JPG, JPEG, and PNG formats are allowed", exception.getMessage());
        verify(fileManagerUtil, times(1)).saveFileAndGetLink(invalidImage);
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost() {
        Long postId = 1L;

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        Post postFromDb = new Post();
        postFromDb.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));

        postService.deletePost(postId);

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).delete(postFromDb);
    }

    @Test
    void deletePostWithInvalidIdAndThrowsPostNotFoundException() {
        Long postId = -1L;

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.deletePost(postId)
        );

        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(postRepository,  never()).delete(any(Post.class));
    }

    @Test
    void checkPostPresentAndGet() {
        Long postId = 1L;

        User author = new User();
        Post post = new Post();
        post.setId(postId);
        post.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        User result = postService.getAuthorFromPostByPostId(postId);

        assertEquals(post.getAuthor(), result);
        verify(postRepository, times(1)).findById(postId);
    }
}