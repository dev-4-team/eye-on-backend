package com.on.eye.api.auth.model;

import com.on.eye.api.auth.model.entity.OauthInfo;
import com.on.eye.api.auth.model.entity.Profile;
import com.on.eye.api.auth.model.enums.AuthProvider;
import com.on.eye.api.auth.model.response.KakaoInformationResponse;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class KaKaoUserInfoDto {
    // oauth인증한 사용자 고유 아이디
    private final String oauthId;

    private final String email;
    private final String profileImage;
    private final String nickname;

    private final AuthProvider authProvider;

    public static KaKaoUserInfoDto from(KakaoInformationResponse response) {
        return KaKaoUserInfoDto.builder()
                .authProvider(AuthProvider.KAKAO)
                .oauthId(response.getId())
                .profileImage(response.getProperties().getProfileImage())
                .nickname(response.getProperties().getNickname())
                .build();
    }

    public Profile toProfile() {
        return Profile.builder().email("").nickname(nickname).profileImageUrl(profileImage).build();
    }

    public OauthInfo toOAuthInfo() {
        return OauthInfo.builder().authProvider(authProvider).providerId(oauthId).build();
    }
}
