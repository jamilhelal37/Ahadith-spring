package com.jamil.ahadith.features.notification.dto.request;

import com.jamil.ahadith.features.notification.entity.Notification;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import com.jamil.ahadith.features.hadith.entity.Hadith;
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
