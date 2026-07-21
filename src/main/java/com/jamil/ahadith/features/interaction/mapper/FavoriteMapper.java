package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;
import com.jamil.ahadith.features.interaction.dto.response.FavoriteResponseDto;
import com.jamil.ahadith.features.interaction.entity.Favorite;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FavoriteMapper extends AuditMapping {
    @Mapping(target = "hadith.id", source = "hadith.id")
    @Mapping(target = "hadith.name", source = "hadith.text")
    FavoriteResponseDto toResponseDto(Favorite entity);
}
