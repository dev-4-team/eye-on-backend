package com.on.eye.api.auth.helper;

import com.on.eye.api.auth.jwt.JwtTokenProvider;
import com.on.eye.api.auth.model.dto.ProfileViewDto;
import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.auth.model.response.TokenAndUserResponse;
import com.on.eye.api.global.common.annotation.Helper;

import lombok.RequiredArgsConstructor;

@Helper
@RequiredArgsConstructor
public class TokenGeneratorHelper {
    private final JwtTokenProvider jwtTokenProvider;

    public TokenAndUserResponse execute(User user) {
        String newAccessToken =
                jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().getValue());

        return TokenAndUserResponse.builder()
                .userProfile(ProfileViewDto.from(user))
                .accessToken(newAccessToken)
                .accessTokenAge(jwtTokenProvider.getAccessTokenTTlSecond())
                .build();
    }
}
