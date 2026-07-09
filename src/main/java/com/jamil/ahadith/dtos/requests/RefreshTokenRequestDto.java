package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequestDto {
    @NotBlank
    private String refreshToken;
}
