package com.jamil.ahadith.features.search.dto.response;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookFilterOptionDto {
    private UUID id;
    private String name;
    private SimpleReferenceDto muhaddith;
}
