package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.ExplainingRequestDto;
import com.jamil.ahadith.dtos.responses.ExplainingResponseDto;
import com.jamil.ahadith.dtos.updates.ExplainingUpdateDto;
import com.jamil.ahadith.entities.Explaining;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExplainingMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    Explaining toEntity(ExplainingRequestDto dto);

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    ExplainingResponseDto toResponseDto(Explaining entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    void updateEntity(ExplainingUpdateDto dto, @MappingTarget Explaining entity);
}
