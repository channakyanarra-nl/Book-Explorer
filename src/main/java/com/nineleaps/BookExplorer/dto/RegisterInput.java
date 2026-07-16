package com.nineleaps.BookExplorer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterInput(
        @NotBlank(message = "Name cannot be empty")
        String name,

        @Email(message = "Invalid Format")
        @NotBlank(message = "Email cannot be blank")
        String email,

        @NotBlank(message = "Password cannot be blank")
        String password
) {}
