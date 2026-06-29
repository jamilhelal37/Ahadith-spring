package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Gender;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MuhaddithRequestDto {
    @NotBlank(message = "Muhaddith name is required")
    private String name;
    private Gender gender;
    private String about;
}
