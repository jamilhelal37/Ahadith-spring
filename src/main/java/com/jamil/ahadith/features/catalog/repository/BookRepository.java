package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.dto.projection.PublicBookRow;
import com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Override
    @EntityGraph(attributePaths = "muhaddith")
    Page<Book> findAll(Pageable pageable);

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.projection.PublicBookRow(b.id, b.name, m.id, m.name)
            from Book b
            left join b.muhaddith m
            order by b.name asc, b.id asc
            """)
    List<PublicBookRow> findPublicBookRows();

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.projection.PublicBookRow(b.id, b.name, m.id, m.name)
            from Book b
            left join b.muhaddith m
            where b.id = :id
            """)
    Optional<PublicBookRow> findPublicBookRowById(@Param("id") UUID id);

    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.reference.BookReferenceResponseDto(b.id, b.name)
            from Book b
            order by b.name asc, b.id asc
            """)
    List<BookReferenceResponseDto> findAllBookReferences();
}
