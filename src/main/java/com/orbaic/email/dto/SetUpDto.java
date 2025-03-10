package com.orbaic.email.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SetUpDto {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Min(value = 10, message = "sendingLimit must be greater than or 10")
    @Max(value = 100, message = "sendingLimit must be less than or 100")
    private Integer sendingLimit;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean sendingEnabled;
}
