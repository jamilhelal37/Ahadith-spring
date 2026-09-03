package com.jamil.ahadith.features.user.dto.request;

import com.jamil.ahadith.features.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserStatusUpdateRequestDto {
    @NotNull(message = "Status is required")
    private UserStatus status;
}
