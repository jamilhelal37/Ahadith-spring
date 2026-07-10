package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    @NotBlank
    private String token;

    @NotBlank
    private String newPassword;
}
