package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.BookRequestDto;
import com.jamil.ahadith.dtos.responses.BookResponseDto;
import com.jamil.ahadith.dtos.updates.BookUpdateDto;
import com.jamil.ahadith.entities.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {
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
