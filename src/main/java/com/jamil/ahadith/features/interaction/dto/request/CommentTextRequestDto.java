package com.jamil.ahadith.features.interaction.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentTextRequestDto {
    @NotBlank(message = "Comment text is required")
    @Size(max = ValidationLimits.COMMENT_MAX, message = "Comment text must not exceed {max} characters")
    private String text;
}
