package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.interaction.dto.request.QuestionRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.QuestionResponseDto;
import com.jamil.ahadith.features.interaction.dto.update.QuestionUpdateDto;
import com.jamil.ahadith.features.interaction.entity.Question;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface QuestionMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "asker", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "answerText", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Question toEntity(QuestionRequestDto dto);

    QuestionResponseDto toResponseDto(Question entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "asker", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(QuestionUpdateDto dto, @MappingTarget Question entity);
}
