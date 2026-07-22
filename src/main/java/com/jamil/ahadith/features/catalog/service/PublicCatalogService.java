package com.jamil.ahadith.features.catalog.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.core.web.dto.PaginationMeta;
import com.jamil.ahadith.core.web.dto.SearchResponse;
import com.jamil.ahadith.core.web.dto.SimpleReferenceDto;
import com.jamil.ahadith.features.catalog.dto.projection.PublicBookRow;
import com.jamil.ahadith.features.catalog.dto.projection.PublicMuhaddithRow;
import com.jamil.ahadith.features.catalog.dto.projection.PublicRawiRow;
import com.jamil.ahadith.features.catalog.dto.response.PublicTextDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicBookResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicMuhaddithListItemDto;
import com.jamil.ahadith.features.catalog.dto.response.publicapi.PublicRawiListItemDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.catalog.exception.MuhaddithNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RawiNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.exception.TopicNotFoundException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.exception.FakeHadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.hadith.repository.FakeHadithRepository;
import com.jamil.ahadith.features.search.dto.response.HadithSearchItemDto;
import com.jamil.ahadith.features.search.service.HadithSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicCatalogService {
    private static final int MAX_TEXT_SIZE = 50;

    private final BookRepository bookRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final TopicRepository topicRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final ExplainingRepository explainingRepository;
    private final FakeHadithRepository fakeHadithRepository;
    private final HadithSearchService hadithSearchService;

    public List<PublicBookResponseDto> getBooks() {
        return bookRepository.findPublicBookRows().stream()
                .map(this::toPublicBookResponseDto)
                .toList();
    }

    public PublicBookResponseDto getBook(UUID id) {
        return bookRepository.findPublicBookRowById(id)
                .map(this::toPublicBookResponseDto)
                .orElseThrow(BookNotFoundException::new);
    }

    public SearchResponse<HadithSearchItemDto> getBookAhadith(UUID bookId, int page, int size) {
        return hadithSearchService.getBookAhadith(bookId, page, size);
    }

    public List<PublicRawiListItemDto> getRawis() {
        return withRawiSerialNumbers(rawiRepository.findPublicRawiRows());
    }

    public SimpleReferenceDto getRawi(UUID id) {
        return rawiRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(RawiNotFoundException::new);
    }

    public List<SimpleReferenceDto> getRulings() {
        return rulingRepository.findAll().stream()
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .toList();
    }

    public SimpleReferenceDto getRuling(UUID id) {
        return rulingRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(RulingNotFoundException::new);
    }

    public List<SimpleReferenceDto> getTopics() {
        return topicRepository.findAll().stream()
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .toList();
    }

    public SimpleReferenceDto getTopic(UUID id) {
        return topicRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(TopicNotFoundException::new);
    }

    public List<PublicMuhaddithListItemDto> getMuhaddiths() {
        return withMuhaddithSerialNumbers(muhaddithRepository.findPublicMuhaddithRows());
    }

    public SimpleReferenceDto getMuhaddith(UUID id) {
        return muhaddithRepository.findById(id)
                .map(item -> new SimpleReferenceDto(item.getId(), item.getName()))
                .orElseThrow(MuhaddithNotFoundException::new);
    }

    public SearchResponse<PublicTextDto> getExplainings(int page, int size) {
        Page<PublicTextDto> items = explainingRepository.findAll(textPageRequest(page, size)).map(item -> new PublicTextDto(item.getId(), item.getText()));
        return response(items);
    }

    public List<PublicTextDto> getExplainings() {
        return explainingRepository.findAll().stream()
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .toList();
    }

    public PublicTextDto getExplaining(UUID id) {
        return explainingRepository.findById(id)
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .orElseThrow(ExplainingNotFoundException::new);
    }

    public SearchResponse<PublicTextDto> getFakeAhadith(int page, int size) {
        Page<PublicTextDto> items = fakeHadithRepository.findAll(textPageRequest(page, size)).map(item -> new PublicTextDto(item.getId(), item.getText()));
        return response(items);
    }

    public List<PublicTextDto> getFakeAhadith() {
        return fakeHadithRepository.findAll().stream()
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .toList();
    }

    public PublicTextDto getFakeHadith(UUID id) {
        return fakeHadithRepository.findById(id)
                .map(item -> new PublicTextDto(item.getId(), item.getText()))
                .orElseThrow(FakeHadithNotFoundException::new);
    }

    private PageRequest textPageRequest(int page, int size) {
        if (page < 0) {
            throw new InvalidRequestException("page must be greater than or equal to 0");
        }
        if (size < 1) {
            throw new InvalidRequestException("size must be greater than 0");
        }
        if (size > MAX_TEXT_SIZE) {
            throw new InvalidRequestException("size must be less than or equal to " + MAX_TEXT_SIZE);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));
    }

    private SearchResponse<PublicTextDto> response(Page<PublicTextDto> page) {
        return new SearchResponse<>(page.getContent(), new PaginationMeta(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        ));
    }

    private List<PublicMuhaddithListItemDto> withMuhaddithSerialNumbers(List<PublicMuhaddithRow> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> new PublicMuhaddithListItemDto(
                        index + 1,
                        items.get(index).getName(),
                        items.get(index).getAbout()))
                .toList();
    }

    private List<PublicRawiListItemDto> withRawiSerialNumbers(List<PublicRawiRow> items) {
        return IntStream.range(0, items.size())
                .mapToObj(index -> new PublicRawiListItemDto(
                        index + 1,
                        items.get(index).getName(),
                        items.get(index).getAbout()))
                .toList();
    }

    private PublicBookResponseDto toPublicBookResponseDto(PublicBookRow row) {
        return new PublicBookResponseDto(
                row.getId(),
                row.getName(),
                toMuhaddithReferenceResponseDto(row));
    }

    private MuhaddithReferenceResponseDto toMuhaddithReferenceResponseDto(PublicBookRow row) {
        if (row.getMuhaddithId() == null) {
            return null;
        }
        return new MuhaddithReferenceResponseDto(row.getMuhaddithId(), row.getMuhaddithName());
    }
}
