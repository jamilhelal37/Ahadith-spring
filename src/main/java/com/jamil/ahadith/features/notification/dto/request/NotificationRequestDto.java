package com.jamil.ahadith.features.notification.dto.request;

import com.jamil.ahadith.features.hadith.dto.request.reference.FakeHadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotificationRequestDto {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "Body is required")
    private String body;
    private String type;
    @Valid
    private HadithReferenceRequestDto hadith;
    @Valid
    private FakeHadithReferenceRequestDto fakeHadith;
}
