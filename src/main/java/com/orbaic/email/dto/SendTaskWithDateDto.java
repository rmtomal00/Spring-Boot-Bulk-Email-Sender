package com.orbaic.email.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SendTaskWithDateDto {

    @NotBlank(message = "Subject can't be empty")
    private String subject;

    @NotNull(message = "BodyId can't be null and should be int")
    private Integer bodyId;

    @NotBlank(message = "Date can't be empty")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
            message = "Date must be in the format yyyy-MM-dd")
    private String startDate;

    @NotNull(message = "Limit can't be null and should be int")
    private Integer limit;

    @NotNull(message = "Skip can't be null and should be int")
    private Integer skip;


    @JsonInclude(JsonInclude.Include.NON_NULL) // Only includes if not null
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$",
            message = "Date must be in the format yyyy-MM-dd")
    private String endDate;

    public String getEndDate() {
        return endDate == null ? "1970-01-01" : endDate;
    }

}
