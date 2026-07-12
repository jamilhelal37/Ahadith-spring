package com.jamil.ahadith.controllers;

import com.jamil.ahadith.dtos.responses.PublicTextDto;
import com.jamil.ahadith.dtos.responses.HadithSearchItemDto;
import com.jamil.ahadith.dtos.responses.PublicBiographyDto;
import com.jamil.ahadith.dtos.responses.PublicBookListItemDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.responses.SimpleReferenceDto;
import com.jamil.ahadith.repositories.BookRepository;
import com.jamil.ahadith.repositories.ExplainingRepository;
import com.jamil.ahadith.repositories.FakeHadithRepository;
import com.jamil.ahadith.repositories.MuhaddithRepository;
import com.jamil.ahadith.repositories.RawiRepository;
import com.jamil.ahadith.repositories.RulingRepository;
import com.jamil.ahadith.repositories.TopicRepository;
import com.jamil.ahadith.services.HadithSearchService;
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
                .orElseThrow(com.jamil.ahadith.exceptions.BookNotFoundException::new);
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
                .orElseThrow(com.jamil.ahadith.exceptions.RawiNotFoundException::new);
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
                .orElseThrow(com.jamil.ahadith.exceptions.RulingNotFoundException::new);
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
                .orElseThrow(com.jamil.ahadith.exceptions.TopicNotFoundException::new);
    }

    @GetMapping("/muhaddiths")
    public List<PublicBiographyDto> getMuhaddiths() {
        return withBiographySerialNumbers(muhaddithRepository.findPublicBiographies());
    }

    @GetMapping("/muhaddiths/{id}")
    public SimpleReferenceDto getMuhaddith(@PathVariable UUID id) {
        return muhaddithRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(com.jamil.ahadith.exceptions.MuhaddithNotFoundException::new);
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
                .orElseThrow(com.jamil.ahadith.exceptions.ExplainingNotFoundException::new);
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
                .orElseThrow(com.jamil.ahadith.exceptions.FakeHadithNotFoundException::new);
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
