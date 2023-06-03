package com.example.social_media_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

public class NewUserDto extends AuthUserDto {
    @JsonProperty("confirm_password")
    @NotBlank(message = "Repeat password cannot be empty")
    private String confirmPassword;
    @JsonProperty("name")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    // Проверка соответствия паролей
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return getPassword().equals(getConfirmPassword());
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
