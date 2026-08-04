package com.jamil.ahadith.features.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyEmailRequestDto {
    @NotBlank(message = "Verification token is required")
    @Size(
            min = 43,
            max = 43,
            message = "Invalid verification token format"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "Invalid verification token format"
    )
    private String token;
}
