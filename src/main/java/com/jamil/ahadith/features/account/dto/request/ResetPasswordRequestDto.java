package com.jamil.ahadith.features.account.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {

    @NotBlank(message = "Password reset token is required")
    @Size(
            min = 43,
            max = 43,
            message = "Invalid password reset token format"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Invalid password reset token format"
    )
    private String token;

    @NotBlank(message = "New password is required")
    @Size(
            min = ValidationLimits.PASSWORD_MIN,
            max = ValidationLimits.PASSWORD_MAX,
            message = "Password must be between {min} and {max} characters"
    )
    private String newPassword;
}
