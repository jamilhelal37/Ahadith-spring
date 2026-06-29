package com.jamil.ahadith.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "search_text")
    private String searchText;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "search_source")
    private SearchSource searchSource;


    @Column(name = "created_at", updatable = false,insertable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;


}