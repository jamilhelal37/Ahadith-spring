package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Hadith;
import lombok.Data;

@Data
public class SimilarAhadithUpdateDto {
    private Hadith mainHadith;
    private Hadith simHadith;
}
