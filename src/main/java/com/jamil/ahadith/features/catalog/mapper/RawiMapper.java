package com.jamil.ahadith.features.catalog.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;


import com.jamil.ahadith.features.catalog.dto.request.RawiRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.RawiResponseDto;
import com.jamil.ahadith.features.catalog.dto.update.RawiUpdateDto;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RawiMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    Rawi toEntity(RawiRequestDto dto);

    RawiResponseDto toResponseDto(Rawi rawi);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    void updateEntity(RawiUpdateDto dto, @MappingTarget Rawi rawi);
}
