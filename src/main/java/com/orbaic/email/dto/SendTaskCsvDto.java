package com.orbaic.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SendTaskCsvDto {
    @NotEmpty(message = "Subject can't be empty")
    private String subject;
    @NotNull(message = "BodyId can't be empty")
    private Integer bodyId;
    @NotNull(message = "You should send a CSV file")
    private MultipartFile csvFile;
}
