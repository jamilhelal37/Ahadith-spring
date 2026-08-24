package com.jamil.ahadith.features.user.mapper;

import com.jamil.ahadith.features.user.dto.response.AdminUserResponseDto;
import com.jamil.ahadith.features.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
    AdminUserResponseDto toResponseDto(User user);
}
