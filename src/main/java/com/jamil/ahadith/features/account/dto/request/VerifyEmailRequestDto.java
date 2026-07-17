package com.jamil.ahadith.features.account.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequestDto {
    @NotBlank
    private String token;
}
