package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    @Query("SELECT a FROM Article a WHERE a.author.id IN (SELECT us.channel.id FROM UserSubscription us WHERE us.subscriber.id = :userId)")
    Page<Article> findArticlesBySubscribedUsersSortedByDate(@Param("userId") Long userId, Pageable pageable);
}
