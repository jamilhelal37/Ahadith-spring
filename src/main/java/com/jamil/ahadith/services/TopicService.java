package com.jamil.ahadith.services;

import com.jamil.ahadith.dtos.requests.TopicRequestDto;
import com.jamil.ahadith.dtos.responses.TopicResponseDto;
import com.jamil.ahadith.dtos.responses.SearchResponse;
import com.jamil.ahadith.dtos.updates.TopicUpdateDto;
import com.jamil.ahadith.exceptions.TopicNotFoundException;
import com.jamil.ahadith.mappers.TopicMapper;
import com.jamil.ahadith.repositories.TopicRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class TopicService {
    private final TopicRepository topicRepository;
    private final TopicMapper topicMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final AdminPageService adminPageService;

    public SearchResponse<TopicResponseDto> getTopics(Pageable pageable) {
        return adminPageService.response(topicRepository.findAll(pageable).map(topicMapper::toResponseDto));
    }

    public TopicResponseDto getTopicById(UUID id) {
        return topicRepository.findById(id)
                .map(topicMapper::toResponseDto)
                .orElseThrow(TopicNotFoundException::new);
    }

    public TopicResponseDto createTopic(TopicRequestDto request) {
        var topic = topicMapper.toEntity(request);
        currentUserService.getCurrentUser().ifPresent(topic::setCreatedBy);
        topic = topicRepository.saveAndFlush(topic);
        entityManager.refresh(topic);
        return topicMapper.toResponseDto(topic);
    }

    public TopicResponseDto updateTopic(UUID id, TopicUpdateDto request) {
        var topic = topicRepository.findById(id).orElseThrow(TopicNotFoundException::new);
        topicMapper.updateEntity(request, topic);
        currentUserService.getCurrentUser().ifPresent(topic::setUpdatedBy);
        var savedTopic = topicRepository.saveAndFlush(topic);
        entityManager.refresh(savedTopic);
        return topicMapper.toResponseDto(savedTopic);
    }

    public void deleteTopic(UUID id) {
        if (!topicRepository.existsById(id)) {
            throw new TopicNotFoundException();
        }
        topicRepository.deleteById(id);
    }
}
