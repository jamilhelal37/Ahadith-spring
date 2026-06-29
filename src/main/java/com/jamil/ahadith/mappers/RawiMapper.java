package com.jamil.ahadith.mappers;


import com.jamil.ahadith.dtos.requests.RawiRequestDto;
import com.jamil.ahadith.dtos.responses.RawiResponseDto;
import com.jamil.ahadith.dtos.updates.RawiUpdateDto;
import com.jamil.ahadith.entities.Rawi;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface RawiMapper {
    Rawi toEntity(RawiRequestDto dto);
    RawiResponseDto toResponseDto(Rawi rawi);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(RawiUpdateDto dto, @MappingTarget Rawi rawi);
}
