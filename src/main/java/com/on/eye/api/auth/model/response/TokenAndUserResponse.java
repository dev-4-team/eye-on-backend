package com.on.eye.api.auth.model.response;

import com.on.eye.api.auth.model.dto.ProfileViewDto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenAndUserResponse {

    private final String accessToken;

    private final Long accessTokenAge;

    private final String refreshToken;

    private final Long refreshTokenAge;

    private final ProfileViewDto userProfile;
}
