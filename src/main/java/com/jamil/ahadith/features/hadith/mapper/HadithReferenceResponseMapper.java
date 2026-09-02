package com.jamil.ahadith.features.hadith.mapper;

import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;

public final class HadithReferenceResponseMapper {

    private HadithReferenceResponseMapper() {
    }

    public static HadithReferenceResponseDto fromSearchItem(
            HadithSearchItemDto item) {

        return new HadithReferenceResponseDto(
                item.getId(),
                item.getText(),
                item.getNormalText(),
                item.getHadithNumber(),
                item.getType(),
                item.getSanad(),
                item.getBook(),
                item.getRawi(),
                item.getRuling(),
                item.getMuhaddith(),
                item.getTopics(),
                item.isHasExplanation(),
                item.isHasSubValid()
        );
    }
}
