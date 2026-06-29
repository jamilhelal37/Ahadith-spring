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
@Table(name = "ahadith")
public class Hadith {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "sub_valid")
    private Hadith subValid;

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

    @ManyToOne
    @JoinColumn(name = "ruling")
    private Ruling ruling;

    @ManyToOne
    @JoinColumn(name = "rawi")
    private Rawi rawi;

    @ManyToOne
    @JoinColumn(name = "book")
    private Book book;

    @Column(name = "sanad")
    private String sanad;

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

    @OneToMany(mappedBy = "subValid")
    private Set<Hadith> ahadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Comment> comments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "subValid")
    private Set<FakeHadith> fakeHadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Favorite> favorites = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Notification> notifications = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<Question> questions = new LinkedHashSet<>();

    @OneToMany(mappedBy = "mainHadith")
    private Set<SimilarAhadith> mainAhadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "simHadith")
    private Set<SimilarAhadith> similarAhadiths = new LinkedHashSet<>();

    @OneToMany(mappedBy = "hadith")
    private Set<TopicClass> topicClasses = new LinkedHashSet<>();


}