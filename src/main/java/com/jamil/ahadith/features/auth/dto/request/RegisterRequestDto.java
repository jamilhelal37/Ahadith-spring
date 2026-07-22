package com.jamil.ahadith.features.auth.dto.request;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.jamil.ahadith.core.config.FlexibleLocalDateDeserializer;
import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = ValidationLimits.EMAIL_MAX)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = ValidationLimits.PASSWORD_MIN, max = ValidationLimits.PASSWORD_MAX,
            message = "Password must be between 8 and 128 characters")
    private String password;

    private Gender gender;

    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate birthDate;
    @Size(max = ValidationLimits.URL_MAX)
    private String avatarUrl;
}
