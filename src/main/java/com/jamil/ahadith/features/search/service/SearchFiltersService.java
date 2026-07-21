package com.jamil.ahadith.features.search.service;

import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.MuhaddithRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.catalog.repository.TopicRepository;
import com.jamil.ahadith.features.search.dto.response.FiltersListResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchFiltersService {
    private final RulingRepository rulingRepository;
    private final RawiRepository rawiRepository;
    private final MuhaddithRepository muhaddithRepository;
    private final BookRepository bookRepository;
    private final TopicRepository topicRepository;

    public FiltersListResponseDto getFiltersList() {
        return new FiltersListResponseDto(
                rulingRepository.findAllRulingReferences(),
                rawiRepository.findAllRawiReferences(),
                muhaddithRepository.findAllMuhaddithReferences(),
                bookRepository.findAllBookReferences(),
                topicRepository.findAllTopicReferences());
    }
}
