package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Muhaddith;
import lombok.Data;

@Data
public class BookUpdateDto {
    private String name;
    private Muhaddith muhaddith;
}
