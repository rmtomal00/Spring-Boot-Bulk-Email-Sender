package com.orbaic.email.dto;

import com.orbaic.email.customValidator.interfaces.ValidEnum;
import com.orbaic.email.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class SetAdminDto {
    @NotEmpty(message = "Email can't be empty")
    @Email(message = "Email type error. It should contain with @ and extension like @gmail.com")
    private String email;

    @ValidEnum(value = Role.class, message = "Invalid Role")
    private Role role;
}
