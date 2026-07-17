package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.interaction.dto.request.CommentRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.CommentResponseDto;
import com.jamil.ahadith.features.interaction.dto.update.CommentUpdateDto;
import com.jamil.ahadith.features.interaction.entity.Comment;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CommentMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentRequestDto dto);

    @Mapping(target = "hadith.id", source = "hadith.id")
    @Mapping(target = "hadith.name", source = "hadith.text")
    CommentResponseDto toResponseDto(Comment entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(CommentUpdateDto dto, @MappingTarget Comment entity);
}
