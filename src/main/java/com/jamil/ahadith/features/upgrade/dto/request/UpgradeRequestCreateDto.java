package com.jamil.ahadith.features.upgrade.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
public class UpgradeRequestCreateDto {
    private MultipartFile document;

    @Size(max = ValidationLimits.NOTES_MAX)
    private String notes;
}
