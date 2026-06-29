package com.jamil.ahadith.mappers;

import com.jamil.ahadith.dtos.requests.FavoriteRequestDto;
import com.jamil.ahadith.dtos.responses.FavoriteResponseDto;
import com.jamil.ahadith.entities.Favorite;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "hadith", source = "hadith")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Favorite toEntity(FavoriteRequestDto dto);

    FavoriteResponseDto toResponseDto(Favorite entity);
}
