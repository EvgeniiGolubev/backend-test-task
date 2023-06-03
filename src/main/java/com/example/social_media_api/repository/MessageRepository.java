package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findBySenderAndReceiverOrReceiverAndSenderOrderByTimestamp(User sender, User receiver, User sender1, User receiver1);
}
