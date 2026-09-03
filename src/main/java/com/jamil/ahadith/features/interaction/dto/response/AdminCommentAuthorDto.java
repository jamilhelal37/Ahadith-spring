package com.jamil.ahadith.features.interaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentAuthorDto {
    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;
    private String type;
    private String status;
}
