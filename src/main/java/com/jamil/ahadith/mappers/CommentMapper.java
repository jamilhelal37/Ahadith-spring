package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.CommentRequestDto;
import com.jamil.ahadith.dtos.responses.CommentResponseDto;
import com.jamil.ahadith.dtos.updates.CommentUpdateDto;
import com.jamil.ahadith.entities.Comment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "hadith", source = "hadith")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentRequestDto dto);

    CommentResponseDto toResponseDto(Comment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CommentUpdateDto dto, @MappingTarget Comment entity);
}
