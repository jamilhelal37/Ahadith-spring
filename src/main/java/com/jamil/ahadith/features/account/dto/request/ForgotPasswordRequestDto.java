package com.jamil.ahadith.features.account.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ForgotPasswordRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(
            max = ValidationLimits.EMAIL_MAX,
            message = "Email must not exceed {max} characters"
    )
    private String email;
}
