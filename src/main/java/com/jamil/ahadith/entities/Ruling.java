package com.jamil.ahadith.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ruling")
public class Ruling {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;


    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "updated_by")
    private User updatedBy;


    @Column(name = "created_at", updatable = false,insertable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;


}