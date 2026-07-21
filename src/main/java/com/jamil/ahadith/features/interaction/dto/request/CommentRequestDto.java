package com.jamil.ahadith.features.interaction.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDto {
    private UUID hadithId;
    @NotBlank(message = "Comment text is required")
    private String text;
}
