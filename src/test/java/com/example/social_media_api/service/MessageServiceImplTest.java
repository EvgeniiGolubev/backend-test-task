package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessage() {
        String content = "Hello, World!";
        User sender = new User();
        User receiver = new User();

        Message message = new Message(sender, receiver, content, LocalDateTime.now());

        messageService.sendMessage(sender, receiver, content);

        assertTrue(sender.getSendMessages().contains(message));
        assertTrue(receiver.getReceivedMessages().contains(message));
        verify(messageRepository, times(1)).save(message);
    }

    @Test
    void getMessageHistory() {
        User sender = new User();
        User receiver = new User();

        Message first = new Message(sender, receiver, "Message 1", LocalDateTime.now());
        first.setId(1L);
        Message second = new Message(receiver, sender, "Message 2", LocalDateTime.now());
        second.setId(2L);

        List<Message> messages = new ArrayList<>() {{
            add(first);
            add(second);
        }};

        List<MessageDto> expected = messages.stream().map(MessageDto::new).toList();

        when(messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(
                sender, receiver, sender, receiver)).thenReturn(messages);

        List<MessageDto> messageHistory = messageService.getMessageHistory(sender, receiver);

        assertEquals(expected, messageHistory);
        verify(messageRepository, times(1)).
                findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, sender, receiver);
    }
}