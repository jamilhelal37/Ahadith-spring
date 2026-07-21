package com.jamil.ahadith.features.hadith.dto.request;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExplainingRequestDto {
    @NotBlank(message = "Explaining text is required")
    private String text;
    private String normalText;
    private String searchText;
}
