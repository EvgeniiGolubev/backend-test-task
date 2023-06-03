package com.example.social_media_api.domain.dto;

import com.example.social_media_api.domain.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

public class MessageDto implements Serializable {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("content")
    @NotBlank(message = "Content cannot be empty")
    private String content;
    @JsonProperty("sender")
    private UserDto sender;
    @JsonProperty("receiver")
    private UserDto receiver;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonProperty("create_date")
    private LocalDateTime createDate;

    public MessageDto() {}

    public MessageDto(Message message) {
        this.id = message.getId();
        this.content = message.getContent();
        this.sender = new UserDto(message.getSender());
        this.receiver = new UserDto(message.getReceiver());
        this.createDate = message.getCreateDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserDto getSender() {
        return sender;
    }

    public void setSender(UserDto sender) {
        this.sender = sender;
    }

    public UserDto getReceiver() {
        return receiver;
    }

    public void setReceiver(UserDto receiver) {
        this.receiver = receiver;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }
}
