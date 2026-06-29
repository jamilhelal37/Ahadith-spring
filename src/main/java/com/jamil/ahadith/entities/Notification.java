package com.jamil.ahadith.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @Column(name = "title", nullable = false, length = Integer.MAX_VALUE)
    private String title;

    @NotNull
    @Column(name = "body", nullable = false, length = Integer.MAX_VALUE)
    private String body;

    @Column(name = "type", columnDefinition = "notification_type not null")
    private Object type;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "hadith_id")
    private Hadith hadith;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "fake_hadith_id")
    private FakeHadith fakeHadith;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;


}