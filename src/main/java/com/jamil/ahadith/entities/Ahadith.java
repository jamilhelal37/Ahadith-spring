package com.jamil.ahadith.entities;

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

@Getter
@Setter
@Entity
@Table(name = "ahadith")
public class Ahadith {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "sub_valid")
    private Ahadith subValid;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "explaining")
    private Explaining explaining;

    @ColumnDefault("'marfu'")
    @Column(name = "type", columnDefinition = "hadith_type not null")
    private Object type;

    @NotNull
    @Column(name = "text", nullable = false, length = Integer.MAX_VALUE)
    private String text;

    @Column(name = "normal_text", length = Integer.MAX_VALUE)
    private String normalText;

    @Column(name = "search_text", length = Integer.MAX_VALUE)
    private String searchText;

    @Column(name = "search_vector", columnDefinition = "tsvector")
    private Object searchVector;

    @NotNull
    @Column(name = "hadith_number", nullable = false)
    private Integer hadithNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "ruling")
    private Ruling ruling;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "rawi")
    private Rawi rawi;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "book")
    private Book book;

    @Column(name = "sanad", length = Integer.MAX_VALUE)
    private String sanad;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subValid")
    private Set<Ahadith> ahadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Comment> comments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "subValid")
    private Set<FakeAhadith> fakeAhadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Favorite> favorites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Notification> notifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Question> questions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "mainHadith")
    private Set<SimilarAhadith> MainAhadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "simHadith")
    private Set<SimilarAhadith> similarAhadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<TopicClass> topicClasses = new LinkedHashSet<>();


}