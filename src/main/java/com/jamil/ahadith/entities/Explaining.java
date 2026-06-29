package com.jamil.ahadith.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "explaining")
public class Explaining {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "text")
    private String text;

    @Column(name = "normal_text")
    private String normalText;

    @Column(name = "search_text")
    private String searchText;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at", insertable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false,updatable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "explaining")
    private Set<Hadith> ahadiths = new LinkedHashSet<>();


}