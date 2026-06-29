package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.HadithRequestDto;
import com.jamil.ahadith.dtos.responses.HadithResponseDto;
import com.jamil.ahadith.dtos.updates.HadithUpdateDto;
import com.jamil.ahadith.entities.Hadith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HadithMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "fakeHadiths", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "mainAhadiths", ignore = true)
    @Mapping(target = "similarAhadiths", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    @Mapping(target = "subValid", source = "subValid")
    @Mapping(target = "explaining", source = "explaining")
    @Mapping(target = "ruling", source = "ruling")
    @Mapping(target = "rawi", source = "rawi")
    @Mapping(target = "book", source = "book")
    Hadith toEntity(HadithRequestDto dto);

    HadithResponseDto toResponseDto(Hadith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "fakeHadiths", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "mainAhadiths", ignore = true)
    @Mapping(target = "similarAhadiths", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    @Mapping(target = "subValid", source = "subValid")
    @Mapping(target = "explaining", source = "explaining")
    @Mapping(target = "ruling", source = "ruling")
    @Mapping(target = "rawi", source = "rawi")
    @Mapping(target = "book", source = "book")
    void updateEntity(HadithUpdateDto dto, @MappingTarget Hadith entity);

    default String map(Object value) {
        return value == null ? null : value.toString();
    }
}
