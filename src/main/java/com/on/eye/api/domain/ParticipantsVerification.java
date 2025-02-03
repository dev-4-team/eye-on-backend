package com.on.eye.api.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.on.eye.api.auth.model.entity.User;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "participants_verifications",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"protest_id", "user_id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantsVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protest_id", nullable = false)
    private Protest protest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime verifiedAt = LocalDateTime.now();

    @Builder
    public ParticipantsVerification(Protest protest, User user) {
        this.protest = protest;
        this.user = user;
    }
}
