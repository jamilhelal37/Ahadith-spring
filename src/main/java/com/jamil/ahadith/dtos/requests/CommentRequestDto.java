package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDto {
    private UUID hadithId;
    @NotBlank(message = "Comment text is required")
    private String text;
}
