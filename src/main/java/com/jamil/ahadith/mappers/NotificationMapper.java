package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.NotificationRequestDto;
import com.jamil.ahadith.dtos.responses.NotificationResponseDto;
import com.jamil.ahadith.entities.Notification;
import com.jamil.ahadith.entities.NotificationType;
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
