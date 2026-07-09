package com.jamil.ahadith.dtos.responses;

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
