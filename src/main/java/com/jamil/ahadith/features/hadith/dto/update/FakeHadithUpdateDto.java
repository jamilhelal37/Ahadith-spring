package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import lombok.Data;

@Data
public class FakeHadithUpdateDto {
    private Hadith subValid;
    private String text;
    private String normalText;
    private String searchText;
    private Ruling ruling;
}
