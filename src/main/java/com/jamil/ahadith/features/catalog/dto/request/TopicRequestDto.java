package com.jamil.ahadith.features.catalog.dto.request;

import com.jamil.ahadith.features.catalog.entity.Topic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TopicRequestDto {
    @NotBlank(message = "Topic name is required")
    private String name;
}
