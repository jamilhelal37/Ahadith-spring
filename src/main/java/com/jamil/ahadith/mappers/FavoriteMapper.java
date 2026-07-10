package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.FavoriteRequestDto;
import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.entities.Favorite;
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
