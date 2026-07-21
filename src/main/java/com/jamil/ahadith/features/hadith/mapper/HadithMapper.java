package com.jamil.ahadith.features.hadith.mapper;

import com.jamil.ahadith.features.catalog.entity.Book;
import com.jamil.ahadith.features.catalog.entity.Rawi;
import com.jamil.ahadith.features.catalog.entity.Ruling;
import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RawiReferenceResponseDto;
import com.jamil.ahadith.features.catalog.dto.response.reference.RulingReferenceResponseDto;
import com.jamil.ahadith.features.hadith.entity.Explaining;
import com.jamil.ahadith.features.hadith.dto.response.reference.ExplainingReferenceResponseDto;
import com.jamil.ahadith.features.hadith.dto.response.reference.HadithReferenceResponseDto;
import com.jamil.ahadith.core.web.mapper.AuditMapping;

import com.jamil.ahadith.features.hadith.dto.request.HadithRequestDto;
import com.jamil.ahadith.features.hadith.dto.response.HadithResponseDto;
import com.jamil.ahadith.features.hadith.dto.update.HadithUpdateDto;
import com.jamil.ahadith.features.hadith.entity.Hadith;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface HadithMapper extends AuditMapping {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "fakeHadiths", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "mainAhadiths", ignore = true)
    @Mapping(target = "similarAhadiths", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    @Mapping(target = "subValid", ignore = true)
    @Mapping(target = "explaining", ignore = true)
    @Mapping(target = "ruling", ignore = true)
    @Mapping(target = "rawi", ignore = true)
    @Mapping(target = "book", ignore = true)
    Hadith toEntity(HadithRequestDto dto);

    HadithResponseDto toResponseDto(Hadith entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "searchVector", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "ahadiths", ignore = true)
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "fakeHadiths", ignore = true)
    @Mapping(target = "favorites", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    @Mapping(target = "questions", ignore = true)
    @Mapping(target = "mainAhadiths", ignore = true)
    @Mapping(target = "similarAhadiths", ignore = true)
    @Mapping(target = "topicClasses", ignore = true)
    @Mapping(target = "subValid", ignore = true)
    @Mapping(target = "explaining", ignore = true)
    @Mapping(target = "ruling", ignore = true)
    @Mapping(target = "rawi", ignore = true)
    @Mapping(target = "book", ignore = true)
    void updateEntity(HadithUpdateDto dto, @MappingTarget Hadith entity);

    default HadithReferenceResponseDto toHadithReferenceResponseDto(Hadith hadith) {
        if (hadith == null) {
            return null;
        }
        return new HadithReferenceResponseDto(hadith.getId(), hadith.getHadithNumber(), hadith.getText());
    }

    default ExplainingReferenceResponseDto toExplainingReferenceResponseDto(Explaining explaining) {
        if (explaining == null) {
            return null;
        }
        return new ExplainingReferenceResponseDto(explaining.getId(), explaining.getText());
    }

    default RulingReferenceResponseDto toRulingReferenceResponseDto(Ruling ruling) {
        if (ruling == null) {
            return null;
        }
        return new RulingReferenceResponseDto(ruling.getId(), ruling.getName());
    }

    default RawiReferenceResponseDto toRawiReferenceResponseDto(Rawi rawi) {
        if (rawi == null) {
            return null;
        }
        return new RawiReferenceResponseDto(rawi.getId(), rawi.getName());
    }

    default BookReferenceResponseDto toBookReferenceResponseDto(Book book) {
        if (book == null) {
            return null;
        }
        return new BookReferenceResponseDto(book.getId(), book.getName());
    }
}
