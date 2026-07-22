package com.jamil.ahadith.features.hadith.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;
import com.jamil.ahadith.features.hadith.dto.request.ExplainingRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.ExplainingResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.ExplainingUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Explaining;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ExplainingMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "normalText", ignore = true)
    @Mapping(target = "searchText", ignore = true)
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
    @Mapping(target = "normalText", ignore = true)
    @Mapping(target = "searchText", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    void updateEntity(ExplainingUpdateDto dto, @MappingTarget Explaining entity);
}
