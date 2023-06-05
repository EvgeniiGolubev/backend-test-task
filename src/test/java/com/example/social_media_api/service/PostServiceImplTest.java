package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.domain.dto.UserDto;
import com.example.social_media_api.domain.entity.Post;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.PostNotFoundException;
import com.example.social_media_api.repository.PostRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

class PostServiceImplTest {
    @InjectMocks
    private PostServiceImpl postService;
    @Mock
    private UserService userService;
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserDetailsImpl authenticatedUser;

    private static final String UPLOAD_PATH = "C:/Users/79523/Desktop/testProject/Social_Media_API/uploads";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findAllPosts() {
        Post p = new Post();
        p.setId(1L);
        p.setAuthor(new User());
        Post p2 = new Post();
        p2.setId(1L);
        p2.setAuthor(new User());

        List<Post> posts = new ArrayList<>(Arrays.asList(p, p2));
        List<PostDto> expected = posts.stream().map(PostDto::new).toList();

        when(postRepository.findAll()).thenReturn(posts);

        List<PostDto> result = postService.findAllPosts();

        assertEquals(expected, result);

        verify(postRepository, times(1)).findAll();
    }

    @Test
    void getPostsBySubscriberWithValidParams() {
        Post p = new Post();
        p.setId(1L);
        p.setAuthor(new User());
        Post p2 = new Post();
        p2.setId(1L);
        p2.setAuthor(new User());

        List<Post> posts = new ArrayList<>(Arrays.asList(p, p2));

        String sortType = "ASC";
        int page = 0;
        int pageSize = 10;

        User userFromDb = new User();
        userFromDb.setId(1L);

        Sort sort = Sort.by(Sort.Direction.ASC, "createDate");
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Post> postsPage = new PageImpl<>(posts, pageable, posts.size());

        Page<PostDto> expected = postsPage.map(PostDto::new);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(userFromDb);
        when(postRepository.findPostsBySubscribedUsersSortedByDate(userFromDb.getId(), pageable)).thenReturn(postsPage);

        Page<PostDto> resultPage = postService.getPostsBySubscriber(authenticatedUser, sortType, page, pageSize);

        assertEquals(expected, resultPage);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, times(1)).findPostsBySubscribedUsersSortedByDate(userFromDb.getId(), pageable);
    }

    @Test
    void getPostsBySubscriberWithInvalidParamSortTypeNullAndThrowsIllegalArgumentException() {
        String sortType = null;
        int page = 0;
        int pageSize = 10;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(authenticatedUser, sortType, page, pageSize)
        );
        assertEquals("Sort type cannot be null", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidParamSortTypeAndThrowsIllegalArgumentException() {
        String sortType = "ERROR";
        int page = 0;
        int pageSize = 10;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(authenticatedUser, sortType, page, pageSize)
        );
        assertEquals("Invalid sortType value! Must be 'DESC' or 'ASC", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidParamPageAndThrowsIllegalArgumentException() {
        String sortType = "ASC";
        int page = -1;
        int pageSize = 10;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(authenticatedUser, sortType, page, pageSize)
        );
        assertEquals("Page number must be non-negative", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).findPostsBySubscribedUsersSortedByDate(anyLong(), any(Pageable.class));
    }

    @Test
    void getPostsBySubscriberWithInvalidParamPageSizeAndThrowsIllegalArgumentException() {
        String sortType = "ASC";
        int page = 1;
        int pageSize = -1;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.getPostsBySubscriber(authenticatedUser, sortType, page, pageSize)
        );
        assertEquals("Page size must be positive", exception.getMessage());


        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
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
        Long postId = 101L;

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
        ReflectionTestUtils.setField(postService, "uploadPath", UPLOAD_PATH);

        PostDto postDto = new PostDto();
        postDto.setId(1L);
        postDto.setTitle("Title");
        postDto.setContent("Content");

        User author = new User();
        author.setId(1L);

        MultipartFile image = new MockMultipartFile("test.png", "test.png", "image/png",
                Files.readAllBytes(Paths.get("src/test/resources/test.png")));

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(author);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(1L);
            return savedPost;
        });

        PostDto result = postService.createPost(postDto, image, authenticatedUser);

        assertEquals(postDto, result);
        assertNotNull(result);
        assertNotNull(result.getTitle());
        assertNotNull(result.getContent());
        assertNotNull(result.getImageLink());
        assertNotNull(result.getAuthor());
        assertNotNull(result.getCreateDate());

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void createPostWithInvalidImageFormatAndThrowsIllegalArgumentException() throws IOException {
        ReflectionTestUtils.setField(postService, "uploadPath", UPLOAD_PATH);

        PostDto postDto = new PostDto();
        postDto.setTitle("Title");
        postDto.setContent("Content");

        MultipartFile invalidImage = new MockMultipartFile("test.gif", "test.gif", "image/gif",
                Files.readAllBytes(Paths.get("src/test/resources/test.gif")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(postDto, invalidImage, authenticatedUser)
        );

        assertEquals("Invalid image format. Only JPG, JPEG, and PNG formats are allowed", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPostWithInvalidPostFieldTitleAndThrowsIllegalArgumentException() {
        PostDto postDto = new PostDto();
        postDto.setTitle("");
        postDto.setContent("Content");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(postDto, null, authenticatedUser)
        );
        assertEquals("Title can not be empty", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void createPostWithInvalidPostFieldContentAndThrowsIllegalArgumentException() {
        PostDto postDto = new PostDto();
        postDto.setTitle("Title");
        postDto.setContent(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.createPost(postDto, null, authenticatedUser)
        );
        assertEquals("Content can not be empty", exception.getMessage());

        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
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
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(author);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PostDto result = postService.updatePost(postId, postDto, image, authenticatedUser);

        PostDto expectedPostDto = new PostDto(postFromDb);

        assertEquals(expectedPostDto, result);

        verify(postRepository, times(1)).findById(postId);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void updatePostWithInvalidIDAndThrowsPostNotFoundException() {
        Long postId = 101L;

        PostDto postDto = new PostDto();
        postDto.setTitle("Updated Title");
        postDto.setContent("Updated Content");
        postDto.setAuthor(new UserDto(new User()));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.updatePost(postId, postDto, null, authenticatedUser)
        );
        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }


    @Test
    void updatePostWithInvalidFieldTitleAndThrowsIllegalArgumentException()  {
        Long postId = 1L;

        PostDto postDto = new PostDto();
        postDto.setTitle(null);
        postDto.setContent("Updated Content");
        postDto.setAuthor(new UserDto(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.updatePost(postId, postDto, null, authenticatedUser)
        );
        assertEquals("Title can not be empty", exception.getMessage());

        verify(postRepository, never()).findById(postId);
        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePostWithInvalidFieldContentAndThrowsIllegalArgumentException()  {
        Long postId = 1L;

        PostDto postDto = new PostDto();
        postDto.setTitle("Updated Title");
        postDto.setContent(" ");
        postDto.setAuthor(new UserDto(new User()));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.updatePost(postId, postDto, null, authenticatedUser)
        );
        assertEquals("Content can not be empty", exception.getMessage());

        verify(postRepository, never()).findById(postId);
        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePostWithInvalidImageFormatAndThrowsIllegalArgumentException() throws IOException {
        ReflectionTestUtils.setField(postService, "uploadPath", UPLOAD_PATH);

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
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(author);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> postService.updatePost(postId, postDto, invalidImage, authenticatedUser)
        );

        assertEquals("Invalid image format. Only JPG, JPEG, and PNG formats are allowed", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void updatePostWithWrongAuthorAndThrowsAccessDeniedException() {

        Long postId = 1L;
        PostDto postDto = new PostDto();
        postDto.setId(postId);
        postDto.setTitle("Title");
        postDto.setContent("Content");
        postDto.setAuthor(new UserDto());

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        Post postFromDb = new Post();
        postFromDb.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(new User());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> postService.updatePost(postId, postDto, null, authenticatedUser)
        );

        assertEquals("Access denied. Only the author can modify or delete the post", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, never()).save(any(Post.class));
    }

    @Test
    void deletePost() throws IOException {
        Long postId = 1L;

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        Post postFromDb = new Post();
        postFromDb.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(author);

        postService.deletePost(postId, authenticatedUser);

        verify(postRepository, times(1)).findById(postId);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository, times(1)).delete(postFromDb);
    }

    @Test
    void deletePostWithInvalidIdAndThrowsPostNotFoundException() throws IOException {
        Long postId = 101L;

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.deletePost(postId, authenticatedUser)
        );

        assertEquals("Post not found", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(userService, never()).getUserFromUserDetails(authenticatedUser);
        verify(postRepository,  never()).delete(any(Post.class));
    }

    @Test
    void deletePostWithWrongAuthorAndThrowsAccessDeniedException() {
        Long postId = 1L;

        User author = new User();
        author.setRoles(Collections.singleton(Role.USER));

        Post postFromDb = new Post();
        postFromDb.setAuthor(author);

        when(postRepository.findById(postId)).thenReturn(Optional.of(postFromDb));
        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(new User());

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> postService.deletePost(postId, authenticatedUser)
        );

        assertEquals("Access denied. Only the author can modify or delete the post", exception.getMessage());

        verify(postRepository, times(1)).findById(postId);
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(postRepository,  never()).delete(postFromDb);
    }
}