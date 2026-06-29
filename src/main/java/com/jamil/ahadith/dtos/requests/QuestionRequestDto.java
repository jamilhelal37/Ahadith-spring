package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class QuestionRequestDto {
    private Hadith hadith;
    private User asker;
    @NotBlank(message = "Question text is required")
    private String askerText;
    private Boolean isActive;
    private String answerText;
}
