package com.jamil.ahadith.features.catalog.dto.response.publicapi;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PublicMuhaddithListItemDto {
    private int serialNumber;
    private String name;
    private String about;
}
