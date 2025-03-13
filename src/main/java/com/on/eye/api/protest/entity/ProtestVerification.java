package com.on.eye.api.protest.entity;

import jakarta.persistence.*;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "protest_verifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProtestVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protest_id", nullable = false)
    private Protest protest;

    private final Integer verifiedNum = 0;

    public ProtestVerification(Protest protest) {
        this.protest = protest;
    }
}
