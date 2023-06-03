package com.example.social_media_api.domain.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
public class Article {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="title", nullable = false)
    private String title;

    @Column(name="content", length = 5000 , nullable = false)
    private String content;

    @Column(name="image_link")
    private String imageLink;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User author;

    @Column(name="create_date", nullable = false)
    private LocalDateTime createDate;

    public Article() {
    }

    public Article(String title, String content, String imageLink, User author, LocalDateTime createDate) {
        this.title = title;
        this.content = content;
        this.imageLink = imageLink;
        this.author = author;
        this.createDate = createDate;
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

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createData) {
        this.createDate = createData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Article article)) return false;

        if (getId() != null ? !getId().equals(article.getId()) : article.getId() != null) return false;
        if (getTitle() != null ? !getTitle().equals(article.getTitle()) : article.getTitle() != null) return false;
        if (getContent() != null ? !getContent().equals(article.getContent()) : article.getContent() != null)
            return false;
        return getImageLink() != null ? getImageLink().equals(article.getImageLink()) : article.getImageLink() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getTitle() != null ? getTitle().hashCode() : 0);
        result = 31 * result + (getContent() != null ? getContent().hashCode() : 0);
        result = 31 * result + (getImageLink() != null ? getImageLink().hashCode() : 0);
        return result;
    }
}
