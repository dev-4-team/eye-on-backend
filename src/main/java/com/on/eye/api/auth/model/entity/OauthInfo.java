package com.on.eye.api.auth.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.on.eye.api.auth.model.enums.AuthProvider;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    @Column(nullable = false, name = "auth_provider")
    @Enumerated(EnumType.STRING)
    private AuthProvider authProvider;

    @Column(nullable = false, name = "provider_id")
    private String providerId;

    @Builder
    public OauthInfo(AuthProvider authProvider, String providerId) {
        this.authProvider = authProvider;
        this.providerId = providerId;
    }

    public OauthInfo withDraw() {
        return OauthInfo.builder()
                .authProvider(this.authProvider)
                .providerId("DELETED" + LocalDateTime.now() + ":" + providerId)
                .build();
    }
}
