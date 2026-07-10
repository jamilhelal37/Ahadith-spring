package com.jamil.ahadith.dtos.requests;

import lombok.Data;

@Data
public class LogoutRequestDto {
    private String refreshToken;
}
