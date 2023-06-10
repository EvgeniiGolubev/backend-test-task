package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void sendMessage(User sender, User receiver, String content) {
        Message message = new Message(sender, receiver, content, LocalDateTime.now());
        sender.getSendMessages().add(message);
        receiver.getReceivedMessages().add(message);

        messageRepository.save(message);
    }

    @Override
    public List<MessageDto> getMessageHistory(User sender, User receiver) {
        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, sender, receiver)
                .stream().map(MessageDto::new).toList();
    }
}
