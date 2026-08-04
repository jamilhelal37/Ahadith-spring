package com.jamil.ahadith.features.interaction.mapper;

import com.jamil.ahadith.core.web.mapper.AuditMapping;
import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import com.jamil.ahadith.features.interaction.dto.request.CommentTextRequestDto;
import com.jamil.ahadith.features.interaction.dto.response.*;
import com.jamil.ahadith.features.interaction.entity.Comment;
import com.jamil.ahadith.features.user.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface CommentMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "hadith", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Comment toEntity(CommentTextRequestDto dto);

    @Mapping(target = "scholar", source = "user")
    PublicCommentResponseDto toPublicResponseDto(Comment comment);

    @Mapping(target = "hadith", source = "hadith")
    ScholarCommentResponseDto toScholarResponseDto(Comment comment);

    @Mapping(target = "scholar", source = "user")
    @Mapping(target = "hadith", source = "hadith")
    AdminCommentResponseDto toAdminResponseDto(Comment comment);

    PublicScholarReferenceDto toPublicScholarReferenceDto(User user);

    AdminCommentAuthorDto toAdminCommentAuthorDto(User user);

    @Mapping(target = "text", source = "text")
    HadithCommentReferenceDto toHadithCommentReferenceDto(Hadith hadith);

    default UUID map(Book book) {
        return book != null ? book.getId() : null;
    }
}
