package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.ArticleDto;
import com.example.social_media_api.domain.entity.Article;
import com.example.social_media_api.domain.entity.Role;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.ArticleNotFoundException;
import com.example.social_media_api.repository.ArticleRepository;
import com.example.social_media_api.repository.UserSubscriptionRepository;
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
public class ArticleServiceImpl implements ArticleService {

    @Value("${upload.path}")
    private String uploadPath;
    private final ArticleRepository articleRepository;
    private final UserService userService;

    @Autowired
    public ArticleServiceImpl(ArticleRepository articleRepository, UserService userService) {
        this.articleRepository = articleRepository;
        this.userService = userService;
    }

    @Override
    public List<ArticleDto> findAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDto::new)
                .collect(Collectors.toList());
    }

    @Override
    public Page<ArticleDto> getArticlesBySubscriber(UserDetailsImpl user, String sortType, int page, int pageSize)
            throws IllegalArgumentException {
        User userFromDb = userService.findUserByEmail(user.getUsername());

        Sort sort = validPaginationAndGetSort(sortType, page, pageSize);
        Pageable pageable = PageRequest.of(page, pageSize, sort);

        Page<Article> resultPage = articleRepository.findArticlesBySubscribedUsersSortedByDate(userFromDb.getId(), pageable);
        return resultPage.map(ArticleDto::new);
    }

    @Override
    public ArticleDto findArticlesById(Long id) throws ArticleNotFoundException {
        Article articleFromDb = checkArticlePresentAndGet(id);

        return new ArticleDto(articleFromDb);
    }

    @Override
    public ArticleDto createArticle(ArticleDto article, MultipartFile image, UserDetailsImpl author)
            throws IOException, IllegalArgumentException {

        validArticleFields(article);

        User userAuthor = userService.findUserByEmail(author.getUsername());

        String imageLink = saveFileAndGetLink(image);

        Article newArticle = new Article(
                article.getTitle(),
                article.getContent(),
                imageLink,
                userAuthor,
                LocalDateTime.now()
        );

        return new ArticleDto(articleRepository.save(newArticle));
    }

    @Override
    public ArticleDto updateArticle(Long id, ArticleDto article, MultipartFile image, UserDetailsImpl author)
            throws AccessDeniedException, ArticleNotFoundException, IOException, IllegalArgumentException {

        validArticleFields(article);

        Article articleFromDb = checkArticlePresentAndGet(id);

        checkAccess(articleFromDb, author);

        String imageLink = saveFileAndGetLink(image);
        if (imageLink == null) {
            imageLink = article.getImageLink();
        } else {
            deleteFile(article.getImageLink());
        }

        articleFromDb.setTitle(article.getTitle());
        articleFromDb.setContent(article.getContent());
        articleFromDb.setImageLink(imageLink);

        return new ArticleDto(articleRepository.save(articleFromDb));
    }

    @Override
    public void deleteArticle(Long id, UserDetailsImpl author)
            throws AccessDeniedException, ArticleNotFoundException, IOException {

        Article articleFromDb = checkArticlePresentAndGet(id);

        checkAccess(articleFromDb, author);

        deleteFile(articleFromDb.getImageLink());

        articleRepository.delete(articleFromDb);
    }

    private String saveFileAndGetLink(MultipartFile image) throws IOException, IllegalArgumentException {
        if (image != null && !image.getOriginalFilename().isEmpty()) {
            Path path = Paths.get(uploadPath);
            Files.createDirectories(path);

            String uuidFile = UUID.randomUUID().toString();
            String originFileName = image.getOriginalFilename();
            String extension = originFileName.substring(originFileName.lastIndexOf(".")).toLowerCase();

            if (!extension.matches("\\.(jpg|jpeg|png)")) {
                throw new IllegalArgumentException("Invalid image format. Only JPG, JPEG, and PNG formats are allowed.");
            }

            String resultFileName = uuidFile + "." + extension;
            Path filePath = Paths.get(uploadPath, resultFileName);
            image.transferTo(Files.createFile(filePath));

            return resultFileName;
        }

        return null;
    }

    private void deleteFile(String filename) throws IOException {
        Path filePath = Paths.get(uploadPath, filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
        }
    }

    private Article checkArticlePresentAndGet(Long id) throws ArticleNotFoundException {
        Article articleFromDb = articleRepository.findById(id).orElse(null);

        if (articleFromDb == null) {
            throw new ArticleNotFoundException("Article not found!");
        }

        return articleFromDb;
    }

    private void checkAccess(Article articleFromDb, UserDetailsImpl author) throws AccessDeniedException{
        User userAuthor = userService.findUserByEmail(author.getUsername());
        User actualAuthor = articleFromDb.getAuthor();

        if (!userAuthor.equals(actualAuthor) && !actualAuthor.getRoles().contains(Role.ADMIN)) {
            throw new AccessDeniedException("Access denied. Only the author can modify or delete the article!");
        }
    }

    private void validArticleFields(ArticleDto article) throws IllegalArgumentException {
        String title = article.getTitle();
        String content = article.getContent();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title can not be empty!");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content can not be empty!");
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
