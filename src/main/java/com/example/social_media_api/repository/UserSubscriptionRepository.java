package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.Article;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.domain.entity.UserSubscription;
import com.example.social_media_api.domain.entity.UserSubscriptionId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, UserSubscriptionId> {
    List<UserSubscription> findBySubscriber(User user);
    List<UserSubscription> findByChannel(User channel);
    UserSubscription findByChannelAndSubscriber(User channel, User subscriber);
}
