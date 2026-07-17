package com.jamil.ahadith.core.web;

import com.jamil.ahadith.core.web.dto.SearchResponse;

import com.jamil.ahadith.core.web.dto.PaginationMeta;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AdminPageService {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 100;

    public Pageable pageable(Integer page, Integer size, String sort, Set<String> allowedSorts, Sort defaultSort) {
        int safePage = page == null || page < 0 ? 0 : page;
        int safeSize = size == null || size < 1 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        Sort safeSort = defaultSort;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String field = parts[0].trim();
            if (!allowedSorts.contains(field)) {
                throw new InvalidRequestException("Unsupported sort field: " + field);
            }
            Sort.Direction direction = parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
            safeSort = Sort.by(direction, field);
        }
        return PageRequest.of(safePage, safeSize, safeSort);
    }

    public <T> com.jamil.ahadith.core.web.dto.SearchResponse<T> response(Page<T> page) {
        return new com.jamil.ahadith.core.web.dto.SearchResponse<>(
                page.getContent(),
                new com.jamil.ahadith.core.web.dto.PaginationMeta(
                        page.getNumber(),
                        page.getSize(),
                        page.getTotalElements(),
                        page.getTotalPages(),
                        page.hasNext(),
                        page.hasPrevious()));
    }
}
