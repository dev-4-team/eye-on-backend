package com.on.eye.api.auth.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.on.eye.api.auth.model.KaKaoUserInfoDto;
import com.on.eye.api.auth.model.dto.CustomOAuth2User;
import com.on.eye.api.auth.model.entity.OauthInfo;
import com.on.eye.api.auth.model.entity.Profile;
import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.auth.model.response.KakaoInformationResponse;
import com.on.eye.api.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        return processOAuth2User(oAuth2User);
    }

    private CustomOAuth2User processOAuth2User(OAuth2User oAuth2User) {
        KakaoInformationResponse kakaoInformationResponse =
                objectMapper.convertValue(
                        oAuth2User.getAttributes(), KakaoInformationResponse.class);
        KaKaoUserInfoDto kaKaoUserInfoDto = KaKaoUserInfoDto.from(kakaoInformationResponse);
        OauthInfo oAuthInfo = kaKaoUserInfoDto.toOAuthInfo();

        User user =
                userRepository
                        .findByOauthInfo(oAuthInfo)
                        .map(
                                existingUser ->
                                        updateExistingUser(
                                                existingUser, kaKaoUserInfoDto.toProfile()))
                        .orElseGet(() -> registerNewUser(kaKaoUserInfoDto));

        user.login();
        return CustomOAuth2User.create(user, oAuth2User.getAttributes());
    }

    private User registerNewUser(KaKaoUserInfoDto kaKaoUserInfoDto) {
        User user =
                User.builder()
                        .oAuthInfo(kaKaoUserInfoDto.toOAuthInfo())
                        .profile(kaKaoUserInfoDto.toProfile())
                        .build();
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, Profile profile) {
        if (user.getProfile().equals(profile)) return user;
        user.changeProfile(profile);
        return userRepository.save(user);
    }
}
