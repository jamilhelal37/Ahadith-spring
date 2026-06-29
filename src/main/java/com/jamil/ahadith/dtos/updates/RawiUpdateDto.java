package com.jamil.ahadith.dtos.updates;

import com.jamil.ahadith.entities.Gender;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RawiUpdateDto {
    @Size(min = 2, message = "Rawi name must be at least 2 characters")
    @Size(max = 255, message = "Rawi name must be less than 255 characters")
    private String name;

    private Gender gender;

    @Size(min = 10, message = "Rawi about must be at least 10 characters")
    private String about;
}
