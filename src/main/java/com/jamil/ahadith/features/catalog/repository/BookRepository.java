package com.jamil.ahadith.features.catalog.repository;

import com.jamil.ahadith.features.catalog.entity.Muhaddith;

import com.jamil.ahadith.features.catalog.dto.response.PublicBookListItemDto;
import com.jamil.ahadith.features.catalog.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface BookRepository extends JpaRepository<Book, UUID> {
    @Query("""
            select new com.jamil.ahadith.features.catalog.dto.response.PublicBookListItemDto(0, b.id, b.name, m.id, m.name)
            from Book b
            left join b.muhaddith m
            order by b.name asc, b.id asc
            """)
    List<PublicBookListItemDto> findPublicBookListItems();
}
