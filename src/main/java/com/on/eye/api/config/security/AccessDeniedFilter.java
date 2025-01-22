package com.on.eye.api.config.security;

import static com.on.eye.api.constants.Constants.SWAGGER_PATTERNS;
import static com.on.eye.api.exception.GlobalErrorCode.ACCESS_TOKEN_NOT_EXIST;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.PatternMatchUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.on.eye.api.dto.ApiResponse;
import com.on.eye.api.exception.BaseErrorCode;
import com.on.eye.api.exception.CustomCodeException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class AccessDeniedFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String servletPath = request.getServletPath();
        return PatternMatchUtils.simpleMatch(SWAGGER_PATTERNS, servletPath);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomCodeException e) {
            responseToClient(
                    response,
                    getErrorResponse(e.getErrorCode(), request.getRequestURL().toString()));
        } catch (AccessDeniedException e) {
            ApiResponse<Map<String, String>> errorResponse =
                    getErrorResponse(ACCESS_TOKEN_NOT_EXIST, request.getRequestURL().toString());
            responseToClient(response, errorResponse);
        }
    }

    private ApiResponse<Map<String, String>> getErrorResponse(
            BaseErrorCode errorCode, String path) {
        return new ApiResponse<>(errorCode.getErrorReason(), path);
    }

    private void responseToClient(
            HttpServletResponse response, ApiResponse<Map<String, String>> errorResponse)
            throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorResponse.getStatus());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
