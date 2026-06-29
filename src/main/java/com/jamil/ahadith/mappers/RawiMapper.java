package com.jamil.ahadith.mappers;


import com.jamil.ahadith.dtos.requests.RawiRequestDto;
import com.jamil.ahadith.dtos.responses.RawiResponseDto;
import com.jamil.ahadith.dtos.updates.RawiUpdateDto;
import com.jamil.ahadith.entities.Rawi;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface RawiMapper {
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
