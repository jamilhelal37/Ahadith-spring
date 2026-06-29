package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class FavoriteRequestDto {
    @NotNull(message = "User is required")
    private User user;
    @NotNull(message = "Hadith is required")
    private Hadith hadith;
}
