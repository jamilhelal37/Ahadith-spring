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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status")
    private UserStatus status;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender")
    private Gender gender;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type")
    private UserType type;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    @Column(name = "created_at", updatable = false,insertable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}