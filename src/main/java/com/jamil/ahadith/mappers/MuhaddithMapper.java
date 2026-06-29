package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.MuhaddithRequestDto;
import com.jamil.ahadith.dtos.responses.MuhaddithResponseDto;
import com.jamil.ahadith.dtos.updates.MuhaddithUpdateDto;
import com.jamil.ahadith.entities.Muhaddith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MuhaddithMapper {
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
