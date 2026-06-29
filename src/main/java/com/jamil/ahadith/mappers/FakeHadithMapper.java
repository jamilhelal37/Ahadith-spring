package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.FakeHadithRequestDto;
import com.jamil.ahadith.dtos.responses.FakeHadithResponseDto;
import com.jamil.ahadith.dtos.updates.FakeHadithUpdateDto;
import com.jamil.ahadith.entities.FakeHadith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FakeHadithMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subValid", source = "subValid")
    @Mapping(target = "ruling", source = "ruling")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    FakeHadith toEntity(FakeHadithRequestDto dto);

    FakeHadithResponseDto toResponseDto(FakeHadith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subValid", source = "subValid")
    @Mapping(target = "ruling", source = "ruling")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntity(FakeHadithUpdateDto dto, @MappingTarget FakeHadith entity);
}
