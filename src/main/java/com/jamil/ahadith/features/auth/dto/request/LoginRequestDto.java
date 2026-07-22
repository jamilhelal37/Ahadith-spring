package com.jamil.ahadith.features.auth.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = ValidationLimits.EMAIL_MAX)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(max = ValidationLimits.PASSWORD_MAX)
    private String password;
}
