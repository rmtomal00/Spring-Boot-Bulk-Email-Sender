package com.orbaic.email.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UploadDataDto {

    @NotEmpty(message = "Title can't be empty")
    private String title;

    @NotNull(message = "File can't be null")
    private MultipartFile file;
}
