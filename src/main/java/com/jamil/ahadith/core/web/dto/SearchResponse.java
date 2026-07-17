package com.jamil.ahadith.core.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse<T> {
    private List<T> items;
    private PaginationMeta pagination;
}
