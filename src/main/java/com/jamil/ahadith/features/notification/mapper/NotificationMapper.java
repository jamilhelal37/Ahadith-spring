package com.jamil.ahadith.features.notification.mapper;

import com.jamil.ahadith.features.hadith.entity.FakeHadith;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.notification.dto.request.NotificationRequestDto;
import com.jamil.ahadith.features.notification.dto.response.NotificationResponseDto;
import com.jamil.ahadith.features.notification.entity.Notification;
import com.jamil.ahadith.features.notification.entity.NotificationType;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface NotificationMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hadith", source = "hadith")
    @Mapping(target = "fakeHadith", source = "fakeHadith")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Notification toEntity(NotificationRequestDto dto);

    NotificationResponseDto toResponseDto(Notification entity);

    default String map(NotificationType value) {
        return value == null ? null : value.toString();
    }

    default NotificationType map(String value) {
        return value == null || value.isBlank() ? NotificationType.general : NotificationType.valueOf(value);
    }
}
