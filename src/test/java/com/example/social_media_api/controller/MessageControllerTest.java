package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.MessageService;
import com.example.social_media_api.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MessageControllerTest {

    @InjectMocks
    private MessageController messageController;

    @Mock
    private MessageService messageService;

    @Mock
    private UserDetailsImpl authenticatedUser;

    @Mock
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void getMessageHistory() throws UserNotFoundException, AccessDeniedException {
        Long receiverId = 2L;
        List<MessageDto> messages = new ArrayList<>() {{
            add(new MessageDto());
            add(new MessageDto());
        }};

        User sender = new User();
        User receiver = new User();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(anyLong())).thenReturn(receiver);
        when(messageService.getMessageHistory(sender, receiver)).thenReturn(messages);

        ResponseEntity<?> responseEntity = messageController.getMessageHistory(authenticatedUser, receiverId);

        assertEquals(messages, responseEntity.getBody());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(anyLong());
        verify(messageService, times(1)).getMessageHistory(sender, receiver);
    }

    @Test
    public void sendMessage() throws UserNotFoundException, AccessDeniedException {
        Long receiverId = 1L;
        String content = "Hello, how are you?";
        MessageDto messageDto = new MessageDto();
        messageDto.setContent(content);

        User sender = new User();
        User receiver = new User();
        sender.getFriends().add(receiver);
        receiver.getFriends().add(sender);

        when(userService.getUserFromUserDetails(authenticatedUser)).thenReturn(sender);
        when(userService.findUserById(anyLong())).thenReturn(receiver);

        ResponseEntity<?> responseEntity = messageController.sendMessage(authenticatedUser, receiverId, messageDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Message sent successfully", ((ResponseMessage) responseEntity.getBody()).getMessage());

        verify(userService, times(1)).getUserFromUserDetails(authenticatedUser);
        verify(userService, times(1)).findUserById(anyLong());
        verify(messageService, times(1)).sendMessage(sender, receiver, content);

    }
}