package com.example.social_media_api.domain.dto;

import com.example.social_media_api.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "DTO for representing a user")
public class UserDto implements Serializable {

    @Schema(description = "User ID")
    @JsonProperty("id")
    private Long id;

    @Schema(description = "User name")
    @JsonProperty("name")
    private String name;

    public UserDto() {
    }

    public UserDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDto userDto)) return false;

        if (getId() != null ? !getId().equals(userDto.getId()) : userDto.getId() != null) return false;
        return getName() != null ? getName().equals(userDto.getName()) : userDto.getName() == null;
    }

    @Override
    public int hashCode() {
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        return result;
    }
}
