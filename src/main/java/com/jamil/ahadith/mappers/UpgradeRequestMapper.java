package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.UpgradeRequestDto;
import com.jamil.ahadith.dtos.responses.UpgradeRequestResponseDto;
import com.jamil.ahadith.entities.UpgradeRequest;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UpgradeRequestMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    UpgradeRequest toEntity(UpgradeRequestDto dto);

    @Mapping(target = "user", source = "user")
    @Mapping(target = "reviewedBy", source = "reviewedBy")
    UpgradeRequestResponseDto toResponseDto(UpgradeRequest entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "reviewedBy", ignore = true)
    @Mapping(target = "reviewNotes", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "reviewedAt", ignore = true)
    void updateEntity(UpgradeRequestDto dto, @MappingTarget UpgradeRequest entity);
}
