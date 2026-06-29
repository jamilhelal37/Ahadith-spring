package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.RulingRequestDto;
import com.jamil.ahadith.dtos.responses.RulingResponseDto;
import com.jamil.ahadith.dtos.updates.RulingUpdateDto;
import com.jamil.ahadith.entities.Ruling;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RulingMapper {
    Ruling toEntity(RulingRequestDto dto);
    RulingResponseDto toResponseDto(Ruling ruling);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RulingUpdateDto dto, @MappingTarget Ruling ruling);
}
