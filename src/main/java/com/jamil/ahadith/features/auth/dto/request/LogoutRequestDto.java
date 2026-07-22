package com.jamil.ahadith.features.auth.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LogoutRequestDto {
    @Size(max = ValidationLimits.TOKEN_MAX)
    private String refreshToken;
}
