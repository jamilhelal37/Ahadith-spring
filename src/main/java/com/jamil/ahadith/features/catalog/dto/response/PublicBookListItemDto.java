package com.jamil.ahadith.features.catalog.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicBookListItemDto {
    private int serialNumber;
    private UUID id;
    private String name;
    private UUID muhaddithId;
    private String muhaddithName;
}
