package com.jamil.ahadith.features.catalog.dto.update;


import com.jamil.ahadith.features.user.entity.Gender;
import lombok.Data;

@Data
public class MuhaddithUpdateDto {
    private String name;
    private Gender gender;
    private String about;
}
