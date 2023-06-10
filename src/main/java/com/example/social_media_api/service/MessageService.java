package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.User;

import java.util.List;

public interface MessageService {
    void sendMessage(User sender, User receiver, String content);

    List<MessageDto> getMessageHistory(User sender, User receiver);
}
