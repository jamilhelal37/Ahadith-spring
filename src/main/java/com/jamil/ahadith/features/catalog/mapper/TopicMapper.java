package com.jamil.ahadith.features.catalog.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.catalog.dto.request.TopicRequestDto;
import com.jamil.ahadith.features.catalog.dto.response.TopicResponseDto;
import com.jamil.ahadith.features.catalog.dto.update.TopicUpdateDto;
import com.jamil.ahadith.features.catalog.entity.Topic;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TopicMapper extends AuditMapping {
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
