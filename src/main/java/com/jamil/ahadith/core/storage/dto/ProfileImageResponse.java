package com.jamil.ahadith.core.storage.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileImageResponse {
    private String avatarUrl;
    private String avatarPublicId;
}
