package com.jamil.ahadith.features.hadith.service;

import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.TopicReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.HadithViewerStateDto;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicExplanationResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicHadithDetailsDto;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicHadithSummaryResponseDto;
import com.jamil.ahadith.features.hadith.exception.HadithNotFoundException;
import com.jamil.ahadith.features.hadith.repository.HadithRepository;
import com.jamil.ahadith.features.interaction.repository.CommentRepository;
import com.jamil.ahadith.features.interaction.repository.FavoriteRepository;
import com.jamil.ahadith.features.search.dto.projection.HadithSearchRow;
import com.jamil.ahadith.features.user.entity.User;
import com.jamil.ahadith.features.user.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicHadithDetailsService {
    private final HadithRepository hadithRepository;
    private final CommentRepository commentRepository;
    private final FavoriteRepository favoriteRepository;
    private final CurrentUserService currentUserService;

    public PublicHadithDetailsDto getDetails(UUID hadithId) {
        HadithSearchRow row = hadithRepository.findPublicDetailsRowById(hadithId);
        if (row == null) {
            throw new HadithNotFoundException();
        }

        List<TopicReferenceResponseDto> topics = hadithRepository.findPublicTopicReferencesByHadithId(hadithId);
        PublicHadithSummaryResponseDto validAlternative = toValidAlternative(row.getSubValidId());
        long commentsCount = commentRepository.countByHadithId(hadithId);
        HadithViewerStateDto viewerState = toViewerState(hadithId);

        return new PublicHadithDetailsDto(
                row.getId(),
                row.getText(),
                row.getNormalText(),
                row.getHadithNumber(),
                row.getType(),
                row.getSanad(),
                toMuhaddithReference(row),
                toRawiReference(row),
                toBookReference(row),
                toRulingReference(row),
                topics,
                toExplanation(row),
                validAlternative,
                commentsCount,
                viewerState);
    }

    private PublicHadithSummaryResponseDto toValidAlternative(UUID hadithId) {
        if (hadithId == null) {
            return null;
        }
        HadithSearchRow row = hadithRepository.findPublicDetailsRowById(hadithId);
        if (row == null) {
            return null;
        }
        return new PublicHadithSummaryResponseDto(
                row.getId(),
                row.getText(),
                row.getNormalText(),
                row.getHadithNumber(),
                row.getType(),
                row.getSanad(),
                toMuhaddithReference(row),
                toRawiReference(row),
                toBookReference(row),
                toRulingReference(row));
    }

    private PublicExplanationResponseDto toExplanation(HadithSearchRow row) {
        if (row.getExplanationId() == null) {
            return null;
        }
        return new PublicExplanationResponseDto(
                row.getExplanationId(),
                row.getExplanationText(),
                row.getExplanationNormalText());
    }

    private HadithViewerStateDto toViewerState(UUID hadithId) {
        return currentUserService.getCurrentUser()
                .map(User::getId)
                .map(userId -> new HadithViewerStateDto(favoriteRepository.existsByUserIdAndHadithId(userId, hadithId)))
                .orElse(null);
    }

    private MuhaddithReferenceResponseDto toMuhaddithReference(HadithSearchRow row) {
        return row.getMuhaddithId() == null
                ? null
                : new MuhaddithReferenceResponseDto(row.getMuhaddithId(), row.getMuhaddithName());
    }

    private RawiReferenceResponseDto toRawiReference(HadithSearchRow row) {
        return row.getRawiId() == null
                ? null
                : new RawiReferenceResponseDto(row.getRawiId(), row.getRawiName());
    }

    private BookReferenceResponseDto toBookReference(HadithSearchRow row) {
        return row.getBookId() == null
                ? null
                : new BookReferenceResponseDto(row.getBookId(), row.getBookName());
    }

    private RulingReferenceResponseDto toRulingReference(HadithSearchRow row) {
        return row.getRulingId() == null
                ? null
                : new RulingReferenceResponseDto(row.getRulingId(), row.getRulingName());
    }
}
