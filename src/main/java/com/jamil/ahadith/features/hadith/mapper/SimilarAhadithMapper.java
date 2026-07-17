package com.jamil.ahadith.features.hadith.mapper;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.hadith.dto.request.SimilarAhadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.SimilarAhadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.SimilarAhadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.SimilarAhadith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SimilarAhadithMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainHadith", source = "mainHadith")
    @Mapping(target = "simHadith", source = "simHadith")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SimilarAhadith toEntity(SimilarAhadithRequestDto dto);

    SimilarAhadithResponseDto toResponseDto(SimilarAhadith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "mainHadith", source = "mainHadith")
    @Mapping(target = "simHadith", source = "simHadith")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(SimilarAhadithUpdateDto dto, @MappingTarget SimilarAhadith entity);
}
