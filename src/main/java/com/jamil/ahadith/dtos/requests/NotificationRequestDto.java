package com.jamil.ahadith.dtos.requests;

import com.jamil.ahadith.entities.FakeHadith;
import com.jamil.ahadith.entities.Hadith;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class NotificationRequestDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Body is required")
    private String body;
    private String type;
    private Hadith hadith;
    private FakeHadith fakeHadith;
}
