package com.jamil.ahadith.features.interaction.dto.response;

import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HadithCommentReferenceDto {
    private UUID id;
    private Integer hadithNumber;
    private String text;
    private BookReferenceResponseDto book;
}
