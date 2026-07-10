package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.QuestionRequestDto;
import com.jamil.ahadith.dtos.responses.QuestionResponseDto;
import com.jamil.ahadith.dtos.updates.QuestionUpdateDto;
import com.jamil.ahadith.entities.Question;
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
