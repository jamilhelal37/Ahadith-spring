package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.Book;
import com.jamil.ahadith.entities.Muhaddith;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class BookRequestDto {
    @NotBlank(message = "Book name is required")
    @Size(min = 2, message = "Book name must be at least 2 characters")
    private String name;
    @NotNull(message = "Muhaddith is required")
    private Muhaddith muhaddith;
}
