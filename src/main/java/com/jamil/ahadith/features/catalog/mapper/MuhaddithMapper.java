package com.jamil.ahadith.features.catalog.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.catalog.dto.request.MuhaddithRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.MuhaddithResponseDto;
import com.jamil.ahadith.features.catalog.dto.update.MuhaddithUpdateDto;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MuhaddithMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "books", ignore = true)
    Muhaddith toEntity(MuhaddithRequestDto dto);

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    MuhaddithResponseDto toResponseDto(Muhaddith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "books", ignore = true)
    void updateEntity(MuhaddithUpdateDto dto, @MappingTarget Muhaddith entity);
}
