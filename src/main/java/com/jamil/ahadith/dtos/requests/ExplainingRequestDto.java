package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExplainingRequestDto {
    @NotBlank(message = "Explaining text is required")
    private String text;
    private String normalText;
    private String searchText;
}
