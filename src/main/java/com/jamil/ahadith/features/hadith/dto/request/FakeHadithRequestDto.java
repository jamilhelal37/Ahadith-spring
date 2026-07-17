package com.jamil.ahadith.features.hadith.dto.request;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.catalog.entity.Ruling;
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
