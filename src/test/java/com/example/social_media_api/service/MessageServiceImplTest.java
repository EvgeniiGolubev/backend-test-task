package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.repository.MessageRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageServiceImplTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @InjectMocks
    private MessageServiceImpl messageService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendMessageWidthValidParams() {
        Long receiverId = 1L;
        String content = "Hello, World!";
        User sender = new User();
        User receiver = new User();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        Message message = new Message(sender, receiver, content, LocalDateTime.now());

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(receiverId)).thenReturn(receiver);
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        messageService.sendMessage(authenticatedUser, receiverId, content);

        assertTrue(sender.getSendMessages().contains(message));
        assertTrue(receiver.getReceivedMessages().contains(message));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(receiverId);
        verify(messageRepository, times(1)).save(message);
    }

    @Test
    void sendMessageToUserWhoIsNotInFriendsListAndThrowsAccessDeniedException() {
        Long receiverId = 1L;
        String content = "Hello, World!";
        User sender = new User();
        User receiver = new User();

        Message message = new Message(sender, receiver, content, LocalDateTime.now());

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(receiverId)).thenReturn(receiver);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> messageService.sendMessage(authenticatedUser, receiverId, content)
        );

        assertEquals("You can only exchange messages with friends", exception.getMessage());
        assertFalse(sender.getSendMessages().contains(message));
        assertFalse(receiver.getReceivedMessages().contains(message));

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(receiverId);
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void getMessageHistory() {
        Long receiverId = 1L;
        User sender = new User();
        User receiver = new User();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        List<Message> messages = new ArrayList<>() {{
            add(new Message(sender, receiver, "Message 1", LocalDateTime.now()));
            add(new Message(receiver, sender, "Message 2", LocalDateTime.now()));
        }};

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(receiverId)).thenReturn(receiver);

        when(messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(
                sender, receiver, sender, receiver)).thenReturn(messages);


        List<MessageDto> messageHistory = messageService.getMessageHistory(authenticatedUser, receiverId);


        assertEquals(2, messageHistory.size());

        assertEquals("Message 1", messageHistory.get(0).getContent());
        assertEquals("Message 2", messageHistory.get(1).getContent());

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(receiverId);
        verify(messageRepository, times(1)).
                findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, sender, receiver);
    }

    @Test
    void getMessageIfUsersNotFriendsAndThrowsAccessDeniedException() {
        Long receiverId = 1L;
        User sender = new User();
        User receiver = new User();

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(receiverId)).thenReturn(receiver);

        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> messageService.getMessageHistory(authenticatedUser, receiverId)
        );

        assertEquals("You can only exchange messages with friends", exception.getMessage());

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(receiverId);
        verify(messageRepository, never())
                .findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, sender, receiver);
    }
}