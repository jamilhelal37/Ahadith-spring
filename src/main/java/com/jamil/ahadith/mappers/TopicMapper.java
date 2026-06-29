package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.TopicRequestDto;
import com.jamil.ahadith.dtos.responses.TopicResponseDto;
import com.jamil.ahadith.dtos.updates.TopicUpdateDto;
import com.jamil.ahadith.entities.Topic;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    Topic toEntity(TopicRequestDto dto);

    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    TopicResponseDto toResponseDto(Topic entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    void updateEntity(TopicUpdateDto dto, @MappingTarget Topic entity);
}
