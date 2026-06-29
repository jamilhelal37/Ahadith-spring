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
@Table(name = "user_fcm_tokens")
public class UserFcmToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @Column(name = "fcm_token", nullable = false, length = Integer.MAX_VALUE)
    private String fcmToken;

    @Column(name = "created_at", updatable = false,insertable = false)
    private LocalDateTime createdAt;


    @Column(name = "updated_at",insertable = false, updatable = false)
    private LocalDateTime updatedAt;


    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "last_seen", nullable = false)
    private LocalDateTime lastSeen;


}