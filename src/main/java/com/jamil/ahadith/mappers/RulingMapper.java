package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.entities.Ruling;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RulingMapper {
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
