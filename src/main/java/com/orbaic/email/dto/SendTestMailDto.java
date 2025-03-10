package com.orbaic.email.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendTestMailDto {
    @NotEmpty(message = "Email can't be empty")
    @Email(message = "Email should be contain with @ and domain")
    private String email;

    @NotEmpty(message = "Subject can't be empty")
    private String subject;

    @NotNull(message = "body can't be empty")
    private Integer bodyId;
}
