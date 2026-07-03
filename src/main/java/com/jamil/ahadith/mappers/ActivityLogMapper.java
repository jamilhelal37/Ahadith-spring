package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.responses.ActivityLogResponseDto;
import com.jamil.ahadith.entities.ActivityLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ActivityLogMapper {
    ActivityLogResponseDto toResponseDto(ActivityLog entity);
}
