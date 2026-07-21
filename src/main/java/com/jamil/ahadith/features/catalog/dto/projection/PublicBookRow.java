package com.jamil.ahadith.features.catalog.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicBookRow {
    private UUID id;
    private String name;
    private UUID muhaddithId;
    private String muhaddithName;
}
