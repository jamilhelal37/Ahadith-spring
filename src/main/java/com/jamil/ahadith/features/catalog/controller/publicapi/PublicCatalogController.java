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

    @GetMapping({"/books", "/api/v1/books"})
    public List<PublicBookResponseDto> getBooks() {
        return publicCatalogService.getBooks();
    }

    @GetMapping({"/books/{id}", "/api/v1/books/{id}"})
    public PublicBookResponseDto getBook(@PathVariable UUID id) {
        return publicCatalogService.getBook(id);
    }

    @GetMapping({"/books/{bookId}/ahadith", "/api/v1/books/{bookId}/ahadith"})
    public SearchResponse<HadithSearchItemDto> getBookAhadith(@PathVariable UUID bookId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return publicCatalogService.getBookAhadith(bookId, page, size);
    }

    @GetMapping({"/rawis", "/api/v1/rawis"})
    public List<PublicRawiListItemDto> getRawis() {
        return publicCatalogService.getRawis();
    }

    @GetMapping({"/rawis/{id}", "/api/v1/rawis/{id}"})
    public SimpleReferenceDto getRawi(@PathVariable UUID id) {
        return publicCatalogService.getRawi(id);
    }

    @GetMapping({"/rulings", "/api/v1/rulings"})
    public List<SimpleReferenceDto> getRulings() {
        return publicCatalogService.getRulings();
    }

    @GetMapping({"/rulings/{id}", "/api/v1/rulings/{id}"})
    public SimpleReferenceDto getRuling(@PathVariable UUID id) {
        return publicCatalogService.getRuling(id);
    }

    @GetMapping({"/topics", "/api/v1/topics"})
    public List<SimpleReferenceDto> getTopics() {
        return publicCatalogService.getTopics();
    }

    @GetMapping({"/topics/{id}", "/api/v1/topics/{id}"})
    public SimpleReferenceDto getTopic(@PathVariable UUID id) {
        return publicCatalogService.getTopic(id);
    }

    @GetMapping({"/muhaddiths", "/api/v1/muhaddiths"})
    public List<PublicMuhaddithListItemDto> getMuhaddiths() {
        return publicCatalogService.getMuhaddiths();
    }

    @GetMapping({"/muhaddiths/{id}", "/api/v1/muhaddiths/{id}"})
    public SimpleReferenceDto getMuhaddith(@PathVariable UUID id) {
        return publicCatalogService.getMuhaddith(id);
    }

    @GetMapping({"/explaining", "/api/v1/explaining"})
    public SearchResponse<PublicTextDto> getExplainings(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "20") int size) {
        return publicCatalogService.getExplainings(page, size);
    }

    @GetMapping({"/explaining/{id}", "/api/v1/explaining/{id}"})
    public PublicTextDto getExplaining(@PathVariable UUID id) {
        return publicCatalogService.getExplaining(id);
    }

    }
