package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResendVerificationRequestDto {
    @NotBlank
    @Email
    private String email;
}
