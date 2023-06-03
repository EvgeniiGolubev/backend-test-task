package com.example.social_media_api.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class NewUserDto implements Serializable {
    @JsonProperty("email")
    @Email(message = "Email is not correct")
    @NotBlank(message = "Email cannot be empty")
    private String email;
    @JsonProperty("password")
    @NotBlank(message = "Password cannot be empty")
    private String password;
    @JsonProperty("confirm_password")
    @NotBlank(message = "Repeat password cannot be empty")
    private String confirmPassword;
    @JsonProperty("name")
    @NotBlank(message = "Name cannot be empty")
    private String name;

    // Проверка соответствия паролей
    @AssertTrue(message = "Passwords do not match")
    public boolean isPasswordMatching() {
        return password.equals(confirmPassword);
    }

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
