package com.jamil.ahadith.features.auth.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    @NotBlank
    @Size(
            max = ValidationLimits.TOKEN_MAX,
            message = "Refresh token must not exceed {max} characters"
    )
    private String refreshToken;
}
