package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import lombok.Data;

@Data
public class FakeHadithUpdateDto {
    @Valid
    private HadithReferenceRequestDto subValid;
    private String text;
    private String normalText;
    private String searchText;
    @Valid
    private RulingReferenceRequestDto ruling;
}
