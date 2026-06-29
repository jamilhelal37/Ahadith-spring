package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RulingRequestDto {
    @NotBlank(message = "Ruling name is required")
    @Size(max = 255, message = "Ruling name must be less than 255 characters")
    private String name;
}


