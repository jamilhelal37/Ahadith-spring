package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentRequestDto {
    @NotNull(message = "Hadith is required")
    private Hadith hadith;
    @NotNull(message = "User  is required")
    private User user;
    @NotBlank(message = "Comment text is required")
    private String text;
}
