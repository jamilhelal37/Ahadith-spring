package com.jamil.ahadith.features.catalog.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.catalog.dto.request.RulingRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.RulingResponseDto;
import com.jamil.ahadith.features.catalog.dto.update.RulingUpdateDto;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RulingMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Ruling toEntity(RulingRequestDto dto);

    RulingResponseDto toResponseDto(Ruling ruling);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(RulingUpdateDto dto, @MappingTarget Ruling ruling);
}
