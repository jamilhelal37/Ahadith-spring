package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Gender;
import lombok.Data;

@Data
public class MuhaddithUpdateDto {
    private String name;
    private Gender gender;
    private String about;
}
