package com.jamil.ahadith.features.interaction.dto.request;


import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDto {
    private UUID hadithId;
    @NotBlank(message = "Comment text is required")
    @Size(max = ValidationLimits.COMMENT_MAX)
    private String text;
}
