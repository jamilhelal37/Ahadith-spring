package com.jamil.ahadith.features.catalog.controller.publicapi;

import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.catalog.dto.response.PublicTextDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicBookResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicMuhaddithListItemDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicRawiListItemDto;
import com.jamil.ahadith.features.catalog.service.PublicCatalogService;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class PublicCatalogController {
    private final PublicCatalogService publicCatalogService;

    @GetMapping("/books")
    public List<PublicBookResponseDto> getBooks() {
        return publicCatalogService.getBooks();
    }

    @GetMapping("/books/{id}")
    public PublicBookResponseDto getBook(@PathVariable UUID id) {
        return publicCatalogService.getBook(id);
    }

    @GetMapping("/books/{bookId}/ahadith")
    public SearchResponse<HadithSearchItemDto> getBookAhadith(@PathVariable UUID bookId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return publicCatalogService.getBookAhadith(bookId, page, size);
    }

    @GetMapping("/rawis")
    public List<PublicRawiListItemDto> getRawis() {
        return publicCatalogService.getRawis();
    }

    @GetMapping("/rawis/{id}")
    public SimpleReferenceDto getRawi(@PathVariable UUID id) {
        return publicCatalogService.getRawi(id);
    }

    @GetMapping("/rulings")
    public List<SimpleReferenceDto> getRulings() {
        return publicCatalogService.getRulings();
    }

    @GetMapping("/rulings/{id}")
    public SimpleReferenceDto getRuling(@PathVariable UUID id) {
        return publicCatalogService.getRuling(id);
    }

    @GetMapping("/topics")
    public List<SimpleReferenceDto> getTopics() {
        return publicCatalogService.getTopics();
    }

    @GetMapping("/topics/{id}")
    public SimpleReferenceDto getTopic(@PathVariable UUID id) {
        return publicCatalogService.getTopic(id);
    }

    @GetMapping("/muhaddiths")
    public List<PublicMuhaddithListItemDto> getMuhaddiths() {
        return publicCatalogService.getMuhaddiths();
    }

    @GetMapping("/muhaddiths/{id}")
    public SimpleReferenceDto getMuhaddith(@PathVariable UUID id) {
        return publicCatalogService.getMuhaddith(id);
    }

    @GetMapping("/explaining")
    public List<PublicTextDto> getExplainings() {
        return publicCatalogService.getExplainings();
    }

    @GetMapping("/explaining/{id}")
    public PublicTextDto getExplaining(@PathVariable UUID id) {
        return publicCatalogService.getExplaining(id);
    }

    @GetMapping("/fake-ahadith")
    public List<PublicTextDto> getFakeAhadith() {
        return publicCatalogService.getFakeAhadith();
    }

    @GetMapping("/fake-ahadith/{id}")
    public PublicTextDto getFakeHadith(@PathVariable UUID id) {
        return publicCatalogService.getFakeHadith(id);
    }
}
