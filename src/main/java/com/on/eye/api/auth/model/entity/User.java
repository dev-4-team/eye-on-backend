package com.on.eye.api.auth.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import com.on.eye.api.auth.error.exception.AlreadyDeletedUserException;
import com.on.eye.api.auth.model.enums.AccountState;
import com.on.eye.api.auth.model.enums.Role;
import com.on.eye.api.auth.model.vo.UserProfileVo;
import com.on.eye.api.global.common.model.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "users",
        indexes = {
            @Index(
                    name = "idx_oauth_lookup",
                    columnList = "auth_provider, provider_id",
                    unique = true)
        })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded private Profile profile;

    @Embedded private OauthInfo oauthInfo;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private final Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    private AccountState accountState = AccountState.NORMAL;

    private LocalDateTime lastLoginAt = LocalDateTime.now();

    @Builder
    public User(Profile profile, OauthInfo oAuthInfo) {
        this.profile = profile;
        this.oauthInfo = oAuthInfo;
    }

    public void changeProfile(Profile profile) {
        this.profile = profile;
    }

    public void withDrawUser() {
        if (accountState.equals(AccountState.DELETED)) throw AlreadyDeletedUserException.EXCEPTION;
        this.accountState = AccountState.DELETED;
        profile.withdraw();
        oauthInfo = oauthInfo.withDraw();
    }

    public void login() {
        lastLoginAt = LocalDateTime.now();
    }

    public UserProfileVo toUserProfileVo() {
        return UserProfileVo.from(this);
    }

    public Boolean isDeleted() {
        return accountState.equals(AccountState.DELETED);
    }
}
