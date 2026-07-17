package com.jamil.ahadith.features.catalog.controller.publicapi;

import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;

import com.jamil.ahadith.features.catalog.exception.TopicNotFoundException;

import com.jamil.ahadith.features.hadith.entity.Hadith;

import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;

import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;

import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;

import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;

import com.jamil.ahadith.features.hadith.entity.Explaining;

import com.jamil.ahadith.features.catalog.exception.RawiNotFoundException;

import com.jamil.ahadith.features.catalog.dto.response.PublicTextDto;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.catalog.dto.response.PublicBiographyDto;
import com.jamil.ahadith.features.catalog.dto.response.PublicBookListItemDto;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@RestController
@AllArgsConstructor
@RequestMapping
public class PublicCatalogController {
    private final BookRepository bookRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final TopicRepository topicRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final ExplainingRepository explainingRepository;
    private final FakeHadithRepository fakeHadithRepository;
    private final HadithSearchService hadithSearchService;

    @GetMapping("/books")
    public List<PublicBookListItemDto> getBooks() {
        return withBookSerialNumbers(bookRepository.findPublicBookListItems());
    }

    @GetMapping("/books/{id}")
    public SimpleReferenceDto getBook(@PathVariable UUID id) {
        return bookRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.features.catalog.exception.BookNotFoundException::new);
    }

    @GetMapping("/books/{bookId}/ahadith")
    public SearchResponse<HadithSearchItemDto> getBookAhadith(@PathVariable UUID bookId,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {
        return hadithSearchService.getBookAhadith(bookId, page, size);
    }

    @GetMapping("/rawis")
    public List<PublicBiographyDto> getRawis() {
        return withBiographySerialNumbers(rawiRepository.findPublicBiographies());
    }

    @GetMapping("/rawis/{id}")
    public SimpleReferenceDto getRawi(@PathVariable UUID id) {
        return rawiRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.features.catalog.exception.RawiNotFoundException::new);
    }

    @GetMapping("/rulings")
    public List<SimpleReferenceDto> getRulings() {
        return rulingRepository.findAll().stream()
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .toList();
    }

    @GetMapping("/rulings/{id}")
    public SimpleReferenceDto getRuling(@PathVariable UUID id) {
        return rulingRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.features.catalog.exception.RulingNotFoundException::new);
    }

    @GetMapping("/topics")
    public List<SimpleReferenceDto> getTopics() {
        return topicRepository.findAll().stream()
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .toList();
    }

    @GetMapping("/topics/{id}")
    public SimpleReferenceDto getTopic(@PathVariable UUID id) {
        return topicRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.features.catalog.exception.TopicNotFoundException::new);
    }

    @GetMapping("/muhaddiths")
    public List<PublicBiographyDto> getMuhaddiths() {
        return withBiographySerialNumbers(muhaddithRepository.findPublicBiographies());
    }

    @GetMapping("/muhaddiths/{id}")
    public SimpleReferenceDto getMuhaddith(@PathVariable UUID id) {
        return muhaddithRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException::new);
    }

    @GetMapping("/explaining")
    public List<PublicTextDto> getExplainings() {
        return explainingRepository.findAll().stream()
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .toList();
    }

    @GetMapping("/explaining/{id}")
    public PublicTextDto getExplaining(@PathVariable UUID id) {
        return explainingRepository.findById(id)
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .orElseThrow(com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException::new);
    }

    @GetMapping("/fake-ahadith")
    public List<PublicTextDto> getFakeAhadith() {
        return fakeHadithRepository.findAll().stream()
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .toList();
    }

    @GetMapping("/fake-ahadith/{id}")
    public PublicTextDto getFakeHadith(@PathVariable UUID id) {
        return fakeHadithRepository.findById(id)
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .orElseThrow(com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException::new);
    }

    private List<PublicBiographyDto> withBiographySerialNumbers(List<PublicBiographyDto> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> new PublicBiographyDto(
                        index + 1,
                        items.get(index).getId(),
                        items.get(index).getName(),
                        items.get(index).getAbout()))
                .toList();
    }

    private List<PublicBookListItemDto> withBookSerialNumbers(List<PublicBookListItemDto> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> new PublicBookListItemDto(
                        index + 1,
                        items.get(index).getId(),
                        items.get(index).getName(),
                        items.get(index).getMuhaddithId(),
                        items.get(index).getMuhaddithName()))
                .toList();
    }
}
