package com.example.social_media_api.repository;

import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class MessageRepositoryTest {

    @Mock
    private MessageRepository messageRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testFindBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate() {
        User sender = new User();
        User receiver = new User();
        User sender1 = new User();
        User receiver1 = new User();
        List<Message> messages = new ArrayList<>();
        when(messageRepository
                .findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, receiver, sender1))
                .thenReturn(messages);

        List<Message> result = messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, receiver, sender1);

        assertEquals(messages, result);
    }

    @Test
    public void testSave() {
        Message message = new Message();

        when(messageRepository.save(message)).thenReturn(message);

        Message result = messageRepository.save(message);

        assertEquals(message, result);
    }
}