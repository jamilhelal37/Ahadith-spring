package com.jamil.ahadith.features.auth.dto.request;

import lombok.Data;

@Data
public class LogoutRequestDto {
    private String refreshToken;
}
