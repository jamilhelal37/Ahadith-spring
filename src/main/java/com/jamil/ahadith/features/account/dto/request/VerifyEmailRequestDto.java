package com.jamil.ahadith.features.account.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyEmailRequestDto {
    @NotBlank
    @Size(max = ValidationLimits.TOKEN_MAX)
    private String token;
}
