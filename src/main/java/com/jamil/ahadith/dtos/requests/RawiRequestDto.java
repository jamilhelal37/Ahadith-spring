package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RawiRequestDto {
    @NotBlank(message = "Rawi name is required")
    @Size(min = 2, message = "Rawi name must be at least 2 characters")
    @Size(max = 255, message = "Rawi name must be less than 255 characters")
    private String name;
    @NotNull(message = "Rawi gender is required")
    private Gender gender;
    @NotBlank(message = "Rawi about is required")
    @Size(min = 10, message = "Rawi about must be at least 10 characters")
    private String about;
}
