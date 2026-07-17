package com.jamil.ahadith.features.search.dto.response;

import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HadithFiltersDto {
    private List<SimpleReferenceDto> muhaddiths;
    private List<SimpleReferenceDto> rawis;
    private List<TypeOptionDto> types;
    private List<SimpleReferenceDto> rulings;
    private List<BookFilterOptionDto> books;
    private List<SimpleReferenceDto> topics;
}
