package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);

    User findByName(String name);

    @EntityGraph(attributePaths = {"subscriptions", "subscribers"})
    Optional<User> findById(Long id);
}
