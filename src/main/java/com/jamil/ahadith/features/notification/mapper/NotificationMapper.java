package com.jamil.ahadith.features.notification.mapper;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.dto.response.reference.FakeHadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "fakeHadith", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notification toEntity(NotificationRequestDto dto);

    @Mapping(target = "hadith", ignore = true)
    NotificationResponseDto toResponseDto(Notification entity);

    default String map(NotificationType value) {
        return value == null ? null : value.toString();
    }

    default NotificationType map(String value) {
        return value == null || value.isBlank() ? NotificationType.general : NotificationType.valueOf(value);
    }

    default HadithReferenceResponseDto toHadithReferenceResponseDto(Hadith hadith) {
        if (hadith == null) {
            return null;
        }
        return new HadithReferenceResponseDto(hadith.getId(), hadith.getHadithNumber(), hadith.getText());
    }

    default FakeHadithReferenceResponseDto toFakeHadithReferenceResponseDto(FakeHadith fakeHadith) {
        if (fakeHadith == null) {
            return null;
        }
        return new FakeHadithReferenceResponseDto(fakeHadith.getId(), fakeHadith.getText());
    }
}
