package com.jamil.ahadith.features.audit.mapper;

import com.jamil.ahadith.features.audit.dto.response.ActivityLogResponseDto;
import com.jamil.ahadith.features.audit.entity.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    ActivityLogResponseDto toResponseDto(ActivityLog entity);
}
