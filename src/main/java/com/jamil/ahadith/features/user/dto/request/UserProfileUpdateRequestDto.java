package com.jamil.ahadith.features.user.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.jamil.ahadith.core.config.FlexibleLocalDateDeserializer;
import com.jamil.ahadith.core.validation.MinimumAge;
import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.user.entity.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileUpdateRequestDto {
    @NotBlank(message = "Name is required")
    @Size(max = ValidationLimits.NAME_MAX)
    private String name;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Birth date is required")
    @Past(message = "Birth date must be in the past")
    @MinimumAge(value = 10, message = "User must be older than 10 years")
    @JsonDeserialize(using = FlexibleLocalDateDeserializer.class)
    private LocalDate birthDate;
}
