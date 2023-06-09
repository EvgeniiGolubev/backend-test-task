package com.example.social_media_api.controller;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.dto.PostDto;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.response.ResponseMessage;
import com.example.social_media_api.security.UserDetailsImpl;
import com.example.social_media_api.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Messages", description = "API for managing messages between friends")
@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(
            summary = "Get message history",
            description = "Get message history between authenticated user (sender) and users from his friends list"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Message history received successfully",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = MessageDto.class)))
            ),
            @ApiResponse(
                    responseCode = "400", description = "User not found or attempt to get message history from a user who is not in the friends list",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"You can only exchange messages with friends\" }") })
            )
    })
    @GetMapping("/history/{receiverId}")
    public ResponseEntity<?> getMessageHistory(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl sender,

            @Parameter(description = "receiver ID")
            @PathVariable("receiverId") Long receiverId
    ) {
        List<MessageDto> messages = messageService.getMessageHistory(sender, receiverId);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @Operation(summary = "Send message",
            description = "Send a message from the authenticated user (sender) to the user from his friends list")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200", description = "Message sent successfully",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "User not found or attempt to send a message to the user who is not in the friends list",
                    content = @Content(schema = @Schema(implementation = ResponseMessage.class),
                    examples = { @ExampleObject(value = "{ \"message\": \"User not found\" }") })
            )
    })
    @PostMapping("/send/{receiverId}")
    public ResponseEntity<?> sendMessage(
            @Parameter(hidden = true)
            @AuthenticationPrincipal UserDetailsImpl sender,

            @Parameter(description = "receiver ID")
            @PathVariable("receiverId") Long receiverId,

            @Parameter(schema = @Schema(implementation = MessageDto.class))
            @Valid @RequestBody MessageDto message
    ) {
        messageService.sendMessage(sender, receiverId, message.getContent());
        return new ResponseEntity<>(new ResponseMessage("Message sent successfully"), HttpStatus.OK);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ResponseMessage>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ResponseMessage> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ResponseMessage(error.getField() + ": " + error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
