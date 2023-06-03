package com.example.social_media_api.service;

import com.example.social_media_api.domain.dto.MessageDto;
import com.example.social_media_api.domain.entity.Message;
import com.example.social_media_api.domain.entity.User;
import com.example.social_media_api.exception.AccessDeniedException;
import com.example.social_media_api.exception.UserNotFoundException;
import com.example.social_media_api.repository.MessageRepository;
import com.example.social_media_api.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserService userService;

    @Autowired
    public MessageServiceImpl(MessageRepository messageRepository, UserService userService) {
        this.messageRepository = messageRepository;
        this.userService = userService;
    }

    @Override
    public void sendMessage(UserDetailsImpl user, Long id, String content) {
        User sender = userService.getUserFromUserDetails(user);
        User receiver = userService.findUserById(id);

        checkAccess(sender, receiver);

        Message message = new Message(sender, receiver, content, LocalDateTime.now());
        sender.getSendMessages().add(message);
        receiver.getReceivedMessages().add(message);

        messageRepository.save(message);
    }

    @Override
    public List<MessageDto> getMessageHistory(UserDetailsImpl user, Long id)
            throws UserNotFoundException, AccessDeniedException {

        User sender = userService.getUserFromUserDetails(user);
        User receiver = userService.findUserById(id);

        checkAccess(sender, receiver);

        return messageRepository.findBySenderAndReceiverOrReceiverAndSenderOrderByCreateDate(sender, receiver, sender, receiver)
                .stream().map(MessageDto::new).toList();
    }

    private void checkAccess(User sender, User receiver) throws AccessDeniedException {
        if (!sender.getFriends().contains(receiver)) {
            throw new AccessDeniedException("You can only exchange messages with friends!");
        }
    }
}
