package com.jamil.ahadith.features.hadith.controller;

import com.jamil.ahadith.core.web.AdminPageService;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.features.hadith.dto.response.FakeHadithResponseDto;
import com.jamil.ahadith.features.hadith.service.FakeHadithService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/fake-ahadith")
public class FakeHadithPublicController {

    private final FakeHadithService fakeHadithService;
    private final AdminPageService adminPageService;

    @GetMapping
    public SearchResponse<FakeHadithResponseDto> getFakeAhadith(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort) {

        return fakeHadithService.getFakeAhadith(
                query,
                adminPageService.pageable(
                        page,
                        size,
                        sort,
                        Set.of("createdAt", "updatedAt", "id"),
                        Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by("id"))
                )
        );
    }

    @GetMapping("/{id}")
    public FakeHadithResponseDto getFakeHadithById(@PathVariable UUID id) {
        return fakeHadithService.getFakeHadithById(id);
    }
}