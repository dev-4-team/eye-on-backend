package com.on.eye.api.domain;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "participants_verifications",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"protest_id", "anonymous_user_Id"})})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ParticipantsVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protest_id", nullable = false, updatable = false)
    private Protest protest;

    @Column(name = "anonymous_user_Id", nullable = false, updatable = false)
    private String anonymousUserId;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime verifiedAt = LocalDateTime.now();

    @Builder
    public ParticipantsVerification(Protest protest, String anonymousUserId) {
        this.protest = protest;
        this.anonymousUserId = anonymousUserId;
    }
}
