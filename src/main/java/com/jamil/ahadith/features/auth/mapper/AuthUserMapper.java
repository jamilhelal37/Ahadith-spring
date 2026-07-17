package com.jamil.ahadith.features.auth.mapper;

import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.features.user.entity.Gender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthUserMapper {
    AuthUserDto toDto(User user);

    default String map(UserStatus status) {
        return status == null ? null : status.name();
    }

    default String map(Gender gender) {
        return gender == null ? null : gender.name();
    }

    default String map(UserType type) {
        return type == null ? null : type.name();
    }
}
