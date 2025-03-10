package com.orbaic.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPassDto {
    @NotEmpty(message = "email can't be empty")
    @Email(message = "Please provide with extension like @gmail.com etc.")
    private String email;

    @NotEmpty(message = "Password can't be empty")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).{6,}$",
    message = "Password minimum 6 characters and contain with upper and lower case")
    private String password;
}
