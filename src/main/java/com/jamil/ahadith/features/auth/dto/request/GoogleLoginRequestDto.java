package com.jamil.ahadith.features.auth.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginRequestDto {
    @NotBlank(message = "Google ID token is required")
    @Size(
            max = ValidationLimits.TOKEN_MAX,
            message = "Google ID token must not exceed {max} characters"
    )
    private String idToken;
}
