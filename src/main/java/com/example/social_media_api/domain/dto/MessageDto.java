package com.example.social_media_api.domain.dto;

import com.example.social_media_api.domain.entity.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "DTO representing a message")
public class MessageDto implements Serializable {

    @Schema(description = "Message ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "Message content. The field cannot be empty.")
    @JsonProperty("content")
    @NotBlank(message = "Content cannot be empty")
    private String content;

    @Schema(description = "Sender of the message")
    @JsonProperty("sender")
    private UserDto sender;

    @Schema(description = "Receiver of the message")
    @JsonProperty("receiver")
    private UserDto receiver;

    @Schema(description = "Date and time when the message was created")
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDto that)) return false;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }
}
