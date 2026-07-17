package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.user.entity.User;

import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.interaction.dto.request.FavoriteRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.FavoriteResponseDto;
import com.jamil.ahadith.features.interaction.entity.Favorite;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FavoriteMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Favorite toEntity(FavoriteRequestDto dto);

    @Mapping(target = "hadith.id", source = "hadith.id")
    @Mapping(target = "hadith.name", source = "hadith.text")
    FavoriteResponseDto toResponseDto(Favorite entity);
}
