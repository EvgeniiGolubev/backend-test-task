package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @GetMapping("/history/{receiverId}")
    public ResponseEntity<?> getMessageHistory(@AuthenticationPrincipal UserDetailsImpl sender,
                                               @PathVariable("receiverId") Long receiverId) {
        try {
            List<MessageDto> messages = messageService.getMessageHistory(sender, receiverId);
            return new ResponseEntity<>(messages, HttpStatus.OK);
        } catch (UserNotFoundException | AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/send/{receiverId}")
    public ResponseEntity<?> sendMessage(@AuthenticationPrincipal UserDetailsImpl sender,
                                         @PathVariable("receiverId") Long receiverId,
                                         @Valid @RequestBody MessageDto message) {
        try {
             messageService.sendMessage(sender, receiverId, message.getContent());
            return ResponseEntity.ok("Message sent successfully!");
        } catch (UserNotFoundException | AccessDeniedException e) {
            return new ResponseEntity<>(new ResponseMessage(e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ResponseMessage>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ResponseMessage> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ResponseMessage(error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
