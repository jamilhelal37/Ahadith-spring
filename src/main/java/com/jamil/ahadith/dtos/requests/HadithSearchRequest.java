package com.jamil.ahadith.dtos.requests;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class HadithSearchRequest {
    private String query;
    private SearchMode mode;
    private Boolean includeExplanation;
    private List<UUID> muhaddithIds;
    private List<UUID> rawiIds;
    private List<String> types;
    private List<UUID> rulingIds;
    private List<UUID> bookIds;
    private List<UUID> topicIds;
    private Integer page;
    private Integer size;
    private HadithSearchSort sort;
}
