package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Hadith;
import com.jamil.ahadith.entities.Ruling;
import lombok.Data;

@Data
public class FakeHadithUpdateDto {
    private Hadith subValid;
    private String text;
    private String normalText;
    private String searchText;
    private Ruling ruling;
}
