package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import lombok.Data;

@Data
public class SimilarAhadithUpdateDto {
    private Hadith mainHadith;
    private Hadith simHadith;
}
