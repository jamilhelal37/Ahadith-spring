package com.jamil.ahadith.features.hadith.mapper;

import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.hadith.dto.request.FakeHadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.FakeHadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.FakeHadith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FakeHadithMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subValid", ignore = true)
    @Mapping(target = "ruling", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    FakeHadith toEntity(FakeHadithRequestDto dto);

    FakeHadithResponseDto toResponseDto(FakeHadith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subValid", ignore = true)
    @Mapping(target = "ruling", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateEntity(FakeHadithUpdateDto dto, @MappingTarget FakeHadith entity);

    default HadithReferenceResponseDto toHadithReferenceResponseDto(Hadith hadith) {
        if (hadith == null) {
            return null;
        }
        return new HadithReferenceResponseDto(hadith.getId(), hadith.getHadithNumber(), hadith.getText());
    }

    default RulingReferenceResponseDto toRulingReferenceResponseDto(Ruling ruling) {
        if (ruling == null) {
            return null;
        }
        return new RulingReferenceResponseDto(ruling.getId(), ruling.getName());
    }
}
