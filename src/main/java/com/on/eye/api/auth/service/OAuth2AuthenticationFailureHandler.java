package com.on.eye.api.auth.service;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException {
        log.debug("FAIL: exception: {}", exception.toString(), exception);
        String targetUrl =
                UriComponentsBuilder.fromUriString("/auth/oauth2/callback")
                        .queryParam("error", exception.getLocalizedMessage())
                        .build()
                        .toUriString();
        log.debug("targetUrl: {}", targetUrl);
        System.out.println("---onAuthenticationFailure---" + exception.getCause());
        System.out.println(exception.getMessage());
        for (StackTraceElement ste : exception.getStackTrace()) {
            System.out.println(ste.toString());
        }
        //
        getRedirectStrategy().sendRedirect(request, response, "http://localhost:8080/login/fail");
    }
}
