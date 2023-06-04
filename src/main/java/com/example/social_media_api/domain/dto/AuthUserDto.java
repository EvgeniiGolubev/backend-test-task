package com.example.social_media_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public abstract class AuthUserDto implements Serializable {

    @Schema(description = "User email address. The field cannot be empty and must be in a specific format.",
            example = "user@gmail.com")
    @JsonProperty("email")
    @Email(message = "Email is not correct")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Schema(description = "User password. The field cannot be empty.")
    @JsonProperty("password")
    @NotBlank(message = "Password cannot be empty")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
