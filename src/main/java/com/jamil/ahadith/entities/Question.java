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
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "questions")
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "hadith_id")
    private Hadith hadith;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "asker")
    private User asker;

    @Column(name = "asker_text")
    private String askerText;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "answer_text")
    private String answerText;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @Column(name = "created_at", insertable = false,updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false,updatable = false)
    private LocalDateTime updatedAt;



}