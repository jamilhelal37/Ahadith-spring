package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class HadithRequestDto {
    private Hadith subValid;
    private Explaining explaining;
    private String type;
    @NotBlank(message = "Hadith text is required")
    private String text;
    private String normalText;
    private String searchText;
    @NotNull(message = "Hadith number is required")
    private Integer hadithNumber;
    private Ruling ruling;
    private Rawi rawi;
    private Book book;
    private String sanad;
}
