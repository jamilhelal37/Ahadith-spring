package com.jamil.ahadith.features.catalog.mapper;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.catalog.dto.request.BookRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.BookResponseDto;
import com.jamil.ahadith.features.catalog.dto.update.BookUpdateDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "muhaddith", source = "muhaddith")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    Book toEntity(BookRequestDto dto);

    BookResponseDto toResponseDto(Book entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "muhaddith", source = "muhaddith")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    void updateEntity(BookUpdateDto dto, @MappingTarget Book entity);
}
