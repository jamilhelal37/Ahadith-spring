package com.jamil.ahadith.features.catalog.dto.update;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import lombok.Data;

@Data
public class BookUpdateDto {
    private String name;
    private Muhaddith muhaddith;
}
