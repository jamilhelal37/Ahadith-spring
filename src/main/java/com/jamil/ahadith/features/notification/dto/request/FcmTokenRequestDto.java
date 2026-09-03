package com.jamil.ahadith.features.notification.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FcmTokenRequestDto {
    @NotBlank(message = "FCM token is required")
    @Size(
            max = ValidationLimits.FCM_TOKEN_MAX,
            message = "FCM token must not exceed {max} characters"
    )
    private String fcmToken;
}
