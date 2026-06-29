package com.jamil.ahadith.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
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
@Table(name = "muhaddiths")
public class Muhaddith {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "about")
    private String about;

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

    @OneToMany(mappedBy = "muhaddith")
    private Set<Book> books = new LinkedHashSet<>();


}