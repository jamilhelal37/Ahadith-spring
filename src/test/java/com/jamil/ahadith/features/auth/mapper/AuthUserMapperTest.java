package com.jamil.ahadith.features.auth.mapper;

import com.jamil.ahadith.features.auth.dto.response.AuthUserDto;
import com.jamil.ahadith.features.user.entity.Gender;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.entity.UserStatus;
import com.jamil.ahadith.features.user.entity.UserType;
import com.jamil.ahadith.features.auth.mapper.AuthUserMapper;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthUserMapperTest {

    private final AuthUserMapper mapper =
            Mappers.getMapper(AuthUserMapper.class);

    @Test
    void toDtoShouldMapAuthUserFieldsAndEnumNames() {
        UUID id = UUID.randomUUID();
        LocalDate birthDate = LocalDate.of(1994, 1, 1);
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setAvatarUrl("https://example.com/avatar.png");
        user.setStatus(UserStatus.active);
        user.setGender(Gender.male);
        user.setType(UserType.member);
        user.setBirthDate(birthDate);

        AuthUserDto dto = mapper.toDto(user);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getName()).isEqualTo("Test User");
        assertThat(dto.getEmail()).isEqualTo("test@example.com");
        assertThat(dto.getAvatarUrl()).isEqualTo("https://example.com/avatar.png");
        assertThat(dto.getStatus()).isEqualTo("active");
        assertThat(dto.getGender()).isEqualTo("male");
        assertThat(dto.getType()).isEqualTo("member");
        assertThat(dto.getBirthDate()).isEqualTo(birthDate);
    }

    @Test
    void toDtoShouldAllowNullEnums() {
        User user = new User();

        AuthUserDto dto = mapper.toDto(user);

        assertThat(dto.getStatus()).isNull();
        assertThat(dto.getGender()).isNull();
        assertThat(dto.getType()).isNull();
    }
}
