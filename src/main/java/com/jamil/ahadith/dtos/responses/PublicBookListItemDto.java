package com.jamil.ahadith.dtos.responses;

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
