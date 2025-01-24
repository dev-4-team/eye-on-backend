package com.on.eye.api.auth.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.on.eye.api.auth.jwt.JwtTokenProvider;
import com.on.eye.api.auth.model.dto.CustomOAuth2User;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider tokenProvider;

    @Value("${app.oauth2.redirectUri}")
    private String redirectUrl;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        GrantedAuthority authority = oAuth2User.getAuthorities().iterator().next();

        String token =
                tokenProvider.generateAccessToken(oAuth2User.getId(), authority.getAuthority());

        // 프론트엔드로 JWT 토큰과 함께 리다이렉트
        String targetUrl =
                UriComponentsBuilder.fromUriString(redirectUrl)
                        .queryParam("access_token", token)
                        .build()
                        .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
