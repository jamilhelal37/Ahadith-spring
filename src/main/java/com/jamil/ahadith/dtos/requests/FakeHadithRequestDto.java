package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.Ruling;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class FakeHadithRequestDto {
    private Hadith subValid;
    @NotBlank(message = "Fake hadith text is required")
    private String text;
    private String normalText;
    private String searchText;
    private Ruling ruling;
}
