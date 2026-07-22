package com.jamil.ahadith.features.notification.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.hadith.dto.request.reference.FakeHadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRequestDto {
    @NotBlank(message = "Title is required")
    @Size(max = ValidationLimits.NOTIFICATION_TITLE_MAX)
    private String title;
    @NotBlank(message = "Body is required")
    @Size(max = ValidationLimits.NOTIFICATION_BODY_MAX)
    private String body;
    @NotNull(message = "Notification type is required")
    private NotificationType type;
    @Valid
    private HadithReferenceRequestDto hadith;
    @Valid
    private FakeHadithReferenceRequestDto fakeHadith;
}
