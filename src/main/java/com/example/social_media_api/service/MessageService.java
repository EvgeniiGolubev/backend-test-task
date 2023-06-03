package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.security.UserDetailsImpl;

import java.util.List;

public interface MessageService {
    void sendMessage(UserDetailsImpl user, Long id, String content) throws UserNotFoundException, AccessDeniedException;

    List<MessageDto> getMessageHistory(UserDetailsImpl user, Long id) throws UserNotFoundException, AccessDeniedException;
}
