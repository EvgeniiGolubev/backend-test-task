package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.MessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetMessageHistory() throws UserNotFoundException, AccessDeniedException {
        Long receiverId = 2L;
        List<MessageDto> messages = new ArrayList<>();
        messages.add(new MessageDto());
        messages.add(new MessageDto());

        when(messageService.getMessageHistory(authenticatedUser, receiverId)).thenReturn(messages);

        ResponseEntity<?> responseEntity = messageController.getMessageHistory(authenticatedUser, receiverId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(messages, responseEntity.getBody());

        verify(messageService, times(1)).getMessageHistory(authenticatedUser, receiverId);
    }

    @Test
    public void testSendMessage() throws UserNotFoundException, AccessDeniedException {
        Long receiverId = 2L;
        String content = "Hello, how are you?";
        MessageDto messageDto = new MessageDto();
        messageDto.setContent(content);

        ResponseEntity<?> responseEntity = messageController.sendMessage(authenticatedUser, receiverId, messageDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Message sent successfully", ((ResponseMessage) responseEntity.getBody()).getMessage());

        verify(messageService, times(1)).sendMessage(authenticatedUser, receiverId, content);

    }

    @Test
    public void testHandleValidationException() {
        MessageDto messageDto = new MessageDto();

        FieldError fieldError = new FieldError("messageDto", "content", "Content cannot be empty");

        BindingResult bindingResult = new BeanPropertyBindingResult(messageDto, "messageDto");
        bindingResult.addError(fieldError);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<List<ResponseMessage>> responseEntity = messageController.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(1, responseEntity.getBody().size());
        assertEquals("content: Content cannot be empty", responseEntity.getBody().get(0).getMessage());
    }
}