package com.jamil.ahadith.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequestDto {
    @NotBlank(message = "Topic name is required")
    private String name;
}
