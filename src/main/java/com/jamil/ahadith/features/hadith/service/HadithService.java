package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.core.exception.InvalidRequestException;
import com.jamil.ahadith.features.audit.service.AuditData;
import com.jamil.ahadith.features.audit.service.AuditEventPublisher;
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
import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithPatchDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Explaining;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.hadith.exception.ExplainingNotFoundException;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.mapper.HadithMapper;
import com.jamil.ahadith.features.hadith.repository.ExplainingRepository;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final AuditEventPublisher auditEventPublisher;

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
        auditEventPublisher.publishCreate("ahadith", hadith.getId(), AuditData.snapshot(hadith));
        return hadithMapper.toResponseDto(hadith);
    }

    public HadithResponseDto updateHadith(UUID id, HadithUpdateDto request) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        var oldData = AuditData.snapshot(hadith);
        hadithMapper.updateEntity(request, hadith);
        applyUpdateRelations(request, hadith);
        currentUserService.getCurrentUser().ifPresent(hadith::setUpdatedBy);
        var savedHadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(savedHadith);
        auditEventPublisher.publishUpdate("ahadith", savedHadith.getId(), oldData, AuditData.snapshot(savedHadith));
        return hadithMapper.toResponseDto(savedHadith);
    }

    public HadithResponseDto patchHadith(UUID id, HadithPatchDto request) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        var oldData = AuditData.snapshot(hadith);
        applyPatch(request, hadith);
        currentUserService.getCurrentUser().ifPresent(hadith::setUpdatedBy);
        var savedHadith = hadithRepository.saveAndFlush(hadith);
        entityManager.refresh(savedHadith);
        auditEventPublisher.publishUpdate("ahadith", savedHadith.getId(), oldData, AuditData.snapshot(savedHadith));
        return hadithMapper.toResponseDto(savedHadith);
    }

    public void deleteHadith(UUID id) {
        var hadith = hadithRepository.findById(id).orElseThrow(HadithNotFoundException::new);
        var oldData = AuditData.snapshot(hadith);
        hadithRepository.delete(hadith);
        auditEventPublisher.publishDelete("ahadith", id, oldData);
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
            if (request.getType().getValue() == null) {
                throw new InvalidRequestException("type cannot be null");
            }
            hadith.setType(request.getType().getValue());
        }
        if (request.getText().isDefined()) {
            if (request.getText().getValue() == null) {
                throw new InvalidRequestException("text cannot be null");
            }
            hadith.setText(request.getText().getValue());
        }
        if (request.getHadithNumber().isDefined()) {
            if (request.getHadithNumber().getValue() == null) {
                throw new InvalidRequestException("hadithNumber cannot be null");
            }
            if (request.getHadithNumber().getValue() < 0) {
                throw new InvalidRequestException("hadithNumber must be greater than or equal to 0");
            }
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
