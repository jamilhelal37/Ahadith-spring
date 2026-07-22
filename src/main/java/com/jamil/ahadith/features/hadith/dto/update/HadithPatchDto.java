package com.jamil.ahadith.features.hadith.dto.update;

import com.jamil.ahadith.core.web.dto.TriState;
import com.jamil.ahadith.core.web.dto.TriStateDeserializer;
import com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto;
import com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;

@Data
public class HadithPatchDto {
    @Valid
    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<HadithReferenceRequestDto> subValid = TriState.undefined();

    @Valid
    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<ExplainingReferenceRequestDto> explaining = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<String> type = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<String> text = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<String> normalText = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<String> searchText = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<Integer> hadithNumber = TriState.undefined();

    @Valid
    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<RulingReferenceRequestDto> ruling = TriState.undefined();

    @Valid
    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<RawiReferenceRequestDto> rawi = TriState.undefined();

    @Valid
    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<BookReferenceRequestDto> book = TriState.undefined();

    @JsonDeserialize(using = TriStateDeserializer.class)
    private TriState<String> sanad = TriState.undefined();
}
