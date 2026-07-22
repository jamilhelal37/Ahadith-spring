package com.jamil.ahadith.features.search.dto.request;

import com.jamil.ahadith.core.validation.ValidationLimits;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HadithSearchRequest {
    @Size(max = ValidationLimits.QUERY_MAX)
    private String query;
    private SearchMode mode;
    private Boolean includeExplanation;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<UUID> muhaddithIds;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<UUID> rawiIds;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<HadithType> types;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<UUID> rulingIds;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<UUID> bookIds;
    @Size(max = ValidationLimits.FILTER_LIST_MAX)
    private List<UUID> topicIds;
    @Min(0)
    private Integer page;
    @Min(1)
    @Max(50)
    private Integer size;
    private HadithSearchSort sort;
}
