package com.jamil.ahadith.features.catalog.dto.request;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MuhaddithRequestDto {
    @NotBlank(message = "Muhaddith name is required")
    private String name;
    private Gender gender;
    private String about;
}
