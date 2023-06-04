package com.example.social_media_api.domain.dto;

import com.example.social_media_api.domain.entity.Post;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "DTO for representing a post")
public class PostDto {

    @Schema(description = "Post ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Post title. The field cannot be empty.")
    @JsonProperty("title")
    private String title;

    @Schema(description = "Post content. The field cannot be empty.")
    @JsonProperty("content")
    private String content;

    @Schema(description = "Address of image stored on disk")
    @JsonProperty("imageLink")
    private String imageLink;

    @Schema(description = "Author of the post")
    @JsonProperty("author")
    private UserDto author;

    @Schema(description = "Creation date and time of the post")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("create_date")
    private LocalDateTime createDate;

    public PostDto() {
    }

    public PostDto(Post post) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.imageLink = post.getImageLink();
        this.author = new UserDto(post.getAuthor());
        this.createDate = post.getCreateDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageLink() {
        return imageLink;
    }

    public void setImageLink(String imageLink) {
        this.imageLink = imageLink;
    }

    public UserDto getAuthor() {
        return author;
    }

    public void setAuthor(UserDto author) {
        this.author = author;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
}
