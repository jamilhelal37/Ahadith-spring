package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.user.service.CurrentUserService;

import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.exception.BookNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RawiNotFoundException;
import com.jamil.ahadith.features.catalog.exception.RulingNotFoundException;
import com.jamil.ahadith.features.catalog.repository.BookRepository;
import com.jamil.ahadith.features.catalog.repository.RawiRepository;
import com.jamil.ahadith.features.catalog.repository.RulingRepository;
import com.jamil.ahadith.features.hadith.dto.request.HadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.ExplainingReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.request.reference.HadithReferenceRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.HadithDto;
import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithPatchDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Explaining;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.entity.HadithType;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.HadithMapper;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional
@AllArgsConstructor
@Service
public class HadithService {
    private final HadithRepository hadithRepository;
    private final HadithMapper hadithMapper;
    private final EntityManager entityManager;
    private final CurrentUserService currentUserService;
    private final BookRepository bookRepository;
    private final RawiRepository rawiRepository;
    private final RulingRepository rulingRepository;
    private final ExplainingRepository explainingRepository;

    public List<HadithDto> getAhadith() {
        return hadithRepository.findAllWithRelations().stream()
                .map(this::toHadithDto)
                .toList();
    }

    public HadithResponseDto getHadithById(UUID id) {
        return hadithRepository.findById(id)
                .map(hadithMapper::toResponseDto)
                .orElseThrow(HadithNotFoundException::new);
    }

    public HadithResponseDto createHadith(HadithRequestDto request) {
        var hadith = hadithMapper.toEntity(request);
        applyCreateRelations(request, hadith);
        currentUserService.getCurrentUser().ifPresent(hadith::setCreatedBy);
        hadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(hadith);
        return hadithMapper.toResponseDto(hadith);
    }

    public HadithResponseDto updateHadith(UUID id, HadithUpdateDto request) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        hadithMapper.updateEntity(request, hadith);
        applyUpdateRelations(request, hadith);
        currentUserService.getCurrentUser().ifPresent(hadith::setUpdatedBy);
        var savedHadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(savedHadith);
        return hadithMapper.toResponseDto(savedHadith);
    }

    public HadithResponseDto patchHadith(UUID id, HadithPatchDto request) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        applyPatch(request, hadith);
        currentUserService.getCurrentUser().ifPresent(hadith::setUpdatedBy);
        var savedHadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(savedHadith);
        return hadithMapper.toResponseDto(savedHadith);
    }

    public void deleteHadith(UUID id) {
        if (!hadithRepository.existsById(id)) {
            throw new HadithNotFoundException();
        }
        hadithRepository.deleteById(id);
    }

    private HadithDto toHadithDto(Hadith hadith) {
        HadithDto dto = new HadithDto();
        dto.setId(hadith.getId());
        dto.setText(hadith.getText());
        dto.setHadithNumber(hadith.getHadithNumber());
        dto.setBookName(hadith.getBook() != null ? hadith.getBook().getName() : null);
        dto.setRawiName(hadith.getRawi() != null ? hadith.getRawi().getName() : null);
        dto.setRulingName(hadith.getRuling() != null ? hadith.getRuling().getName() : null);
        dto.setExplainingText(hadith.getExplaining() != null ? hadith.getExplaining().getText() : null);
        return dto;
    }

    private void applyCreateRelations(HadithRequestDto request, Hadith hadith) {
        hadith.setSubValid(resolveHadith(request.getSubValid()));
        hadith.setExplaining(resolveExplaining(request.getExplaining()));
        hadith.setRuling(resolveRuling(request.getRuling()));
        hadith.setRawi(resolveRawi(request.getRawi()));
        hadith.setBook(resolveBook(request.getBook()));
    }

    private void applyUpdateRelations(HadithUpdateDto request, Hadith hadith) {
        if (request.getSubValid() != null) {
            hadith.setSubValid(resolveHadith(request.getSubValid()));
        }
        if (request.getExplaining() != null) {
            hadith.setExplaining(resolveExplaining(request.getExplaining()));
        }
        if (request.getRuling() != null) {
            hadith.setRuling(resolveRuling(request.getRuling()));
        }
        if (request.getRawi() != null) {
            hadith.setRawi(resolveRawi(request.getRawi()));
        }
        if (request.getBook() != null) {
            hadith.setBook(resolveBook(request.getBook()));
        }
    }

    private void applyPatch(HadithPatchDto request, Hadith hadith) {
        if (request.getType().isDefined()) {
            hadith.setType(request.getType().getValue() == null ? null : HadithType.valueOf(request.getType().getValue()));
        }
        if (request.getText().isDefined()) {
            hadith.setText(request.getText().getValue());
        }
        if (request.getNormalText().isDefined()) {
            hadith.setNormalText(request.getNormalText().getValue());
        }
        if (request.getSearchText().isDefined()) {
            hadith.setSearchText(request.getSearchText().getValue());
        }
        if (request.getHadithNumber().isDefined()) {
            hadith.setHadithNumber(request.getHadithNumber().getValue());
        }
        if (request.getSanad().isDefined()) {
            hadith.setSanad(request.getSanad().getValue());
        }
        if (request.getSubValid().isDefined()) {
            hadith.setSubValid(resolveHadith(request.getSubValid().getValue()));
        }
        if (request.getExplaining().isDefined()) {
            hadith.setExplaining(resolveExplaining(request.getExplaining().getValue()));
        }
        if (request.getRuling().isDefined()) {
            hadith.setRuling(resolveRuling(request.getRuling().getValue()));
        }
        if (request.getRawi().isDefined()) {
            hadith.setRawi(resolveRawi(request.getRawi().getValue()));
        }
        if (request.getBook().isDefined()) {
            hadith.setBook(resolveBook(request.getBook().getValue()));
        }
    }

    private Hadith resolveHadith(HadithReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return hadithRepository.findById(reference.getId())
                .orElseThrow(HadithNotFoundException::new);
    }

    private Explaining resolveExplaining(ExplainingReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return explainingRepository.findById(reference.getId())
                .orElseThrow(ExplainingNotFoundException::new);
    }

    private Ruling resolveRuling(com.jamil.ahadith.features.catalog.dto.request.reference.RulingReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return rulingRepository.findById(reference.getId())
                .orElseThrow(RulingNotFoundException::new);
    }

    private Rawi resolveRawi(com.jamil.ahadith.features.catalog.dto.request.reference.RawiReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return rawiRepository.findById(reference.getId())
                .orElseThrow(RawiNotFoundException::new);
    }

    private Book resolveBook(com.jamil.ahadith.features.catalog.dto.request.reference.BookReferenceRequestDto reference) {
        if (reference == null) {
            return null;
        }
        return bookRepository.findById(reference.getId())
                .orElseThrow(BookNotFoundException::new);
    }
}
