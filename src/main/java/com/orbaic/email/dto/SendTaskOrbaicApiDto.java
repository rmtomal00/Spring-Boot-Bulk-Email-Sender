package com.orbaic.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SendTaskOrbaicApiDto {
    @NotEmpty(message = "Subject can't be empty")
    String subject;
    @NotNull(message = "BodyId can't be empty and should be int")
    Integer bodyId;
    @NotNull(message = "Limit can't be empty and should be int")
    Integer limit;
    @NotNull(message = "Offset can't be empty and should be int")
    Integer offset;
}
