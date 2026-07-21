package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.core.web.dto.AdminUserReferenceDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.MuhaddithReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.catalog.entity.Muhaddith;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.hadith.dto.response.publicapi.PublicHadithSummaryResponseDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.interaction.dto.request.QuestionCreateRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.MemberQuestionResponseDto;
import com.jamil.ahadith.features.interaction.dto.response.ScholarQuestionResponseDto;
import com.jamil.ahadith.features.interaction.entity.Question;
import com.jamil.ahadith.features.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class QuestionMapper {
    public Question toEntity(QuestionCreateRequestDto dto) {
        Question question = new Question();
        question.setAskerText(dto.getAskerText());
        return question;
    }

    public MemberQuestionResponseDto toMemberResponseDto(Question question) {
        String visibleAnswer = Boolean.TRUE.equals(question.getIsActive()) ? question.getAnswerText() : null;
        return new MemberQuestionResponseDto(
                question.getId(),
                toHadithSummary(question.getHadith()),
                question.getAskerText(),
                question.getIsActive(),
                visibleAnswer,
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    public ScholarQuestionResponseDto toScholarResponseDto(Question question) {
        return new ScholarQuestionResponseDto(
                question.getId(),
                toHadithSummary(question.getHadith()),
                toAdminUserReferenceDto(question.getAsker()),
                question.getAskerText(),
                question.getIsActive(),
                question.getAnswerText(),
                toAdminUserReferenceDto(question.getUpdatedBy()),
                question.getCreatedAt(),
                question.getUpdatedAt()
        );
    }

    private PublicHadithSummaryResponseDto toHadithSummary(Hadith hadith) {
        if (hadith == null) {
            return null;
        }
        return new PublicHadithSummaryResponseDto(
                hadith.getId(),
                hadith.getText(),
                hadith.getNormalText(),
                hadith.getHadithNumber(),
                hadith.getType() == null ? null : hadith.getType().name(),
                hadith.getSanad(),
                toMuhaddithReference(hadith.getBook() == null ? null : hadith.getBook().getMuhaddith()),
                toRawiReference(hadith.getRawi()),
                toBookReference(hadith.getBook()),
                toRulingReference(hadith.getRuling())
        );
    }

    private AdminUserReferenceDto toAdminUserReferenceDto(User user) {
        if (user == null) {
            return null;
        }
        return new AdminUserReferenceDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getType() == null ? null : user.getType().name()
        );
    }

    private MuhaddithReferenceResponseDto toMuhaddithReference(Muhaddith muhaddith) {
        if (muhaddith == null) {
            return null;
        }
        return new MuhaddithReferenceResponseDto(muhaddith.getId(), muhaddith.getName());
    }

    private RawiReferenceResponseDto toRawiReference(Rawi rawi) {
        if (rawi == null) {
            return null;
        }
        return new RawiReferenceResponseDto(rawi.getId(), rawi.getName());
    }

    private BookReferenceResponseDto toBookReference(Book book) {
        if (book == null) {
            return null;
        }
        return new BookReferenceResponseDto(book.getId(), book.getName());
    }

    private RulingReferenceResponseDto toRulingReference(Ruling ruling) {
        if (ruling == null) {
            return null;
        }
        return new RulingReferenceResponseDto(ruling.getId(), ruling.getName());
    }
}
