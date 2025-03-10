package com.orbaic.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginDto {
    @NotEmpty(message = "Email can't be empty")
    @Email(message = "Email should be contain a @ and domain")
    String email;

    @NotEmpty(message = "Password can't be empty")
    @Size(min = 6, message = "Password has minimum 6 characters and upper and lower case")
    String password;
}
