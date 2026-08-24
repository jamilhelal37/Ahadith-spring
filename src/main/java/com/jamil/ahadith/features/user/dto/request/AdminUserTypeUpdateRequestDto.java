package com.jamil.ahadith.features.user.dto.request;

import com.jamil.ahadith.features.user.entity.UserType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminUserTypeUpdateRequestDto {
    @NotNull(message = "Type is required")
    private UserType type;
}
