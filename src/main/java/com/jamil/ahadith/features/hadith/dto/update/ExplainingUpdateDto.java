package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import lombok.Data;

@Data
public class ExplainingUpdateDto {
    private String text;
    private String normalText;
    private String searchText;
}
