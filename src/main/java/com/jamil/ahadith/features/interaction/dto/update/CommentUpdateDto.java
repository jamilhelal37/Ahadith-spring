package com.jamil.ahadith.features.interaction.dto.update;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentUpdateDto {
    @Size(max = ValidationLimits.COMMENT_MAX)
    private String text;
}
