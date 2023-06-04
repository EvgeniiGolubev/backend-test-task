package com.example.social_media_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotBlank;

@Schema(description = "DTO for user registration")
public class NewUserDto extends AuthUserDto {

    @Schema(description = "User password confirmation. The field cannot be empty and must match the password field.")
    @JsonProperty("confirm_password")
    @NotBlank(message = "Repeat password cannot be empty")
    private String confirmPassword;

    @Schema(description = "User name. The field cannot be empty.")
    @JsonProperty("name")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Schema(description = "Checking password matches")
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
