package com.jamil.ahadith.features.search.dto.response;

import com.jamil.ahadith.features.catalog.entity.Ruling;

import com.jamil.ahadith.features.catalog.entity.Rawi;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.catalog.entity.Book;

import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HadithSearchItemDto {
    private UUID id;
    private String text;
    private Integer hadithNumber;
    private String type;
    private SimpleReferenceDto book;
    private SimpleReferenceDto rawi;
    private SimpleReferenceDto ruling;
    private SimpleReferenceDto muhaddith;
    private List<SimpleReferenceDto> topics;
    private boolean hasExplanation;
}
