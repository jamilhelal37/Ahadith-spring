package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyEmailRequestDto {
    @NotBlank
    private String token;
}
