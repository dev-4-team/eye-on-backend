package com.on.eye.api.auth.model.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;

import com.on.eye.api.global.common.model.vo.ImageVo;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {
    private String email;

    private String nickname;

    @Embedded private ImageVo profileImageUrl;

    public void withdraw() {
        this.nickname = "탈퇴한 유저";
        this.profileImageUrl = null;
        this.email = null;
    }

    @Builder
    public Profile(String nickname, String profileImageUrl, String email) {
        this.nickname = nickname;
        this.profileImageUrl = ImageVo.of(profileImageUrl);
        this.email = email;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profile profile = (Profile) obj;
        return this.nickname.equals(profile.getNickname())
                && this.profileImageUrl.getUrl().equals(profile.getProfileImageUrl().getUrl());
    }

    @Override
    public int hashCode() {
        return this.nickname.hashCode() + this.profileImageUrl.getUrl().hashCode();
    }
}
