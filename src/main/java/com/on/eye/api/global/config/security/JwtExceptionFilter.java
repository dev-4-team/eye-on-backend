package com.on.eye.api.global.config.security;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.on.eye.api.global.dto.ErrorResponse;
import com.on.eye.api.global.error.exception.BaseErrorCode;
import com.on.eye.api.global.error.exception.CustomCodeException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (CustomCodeException e) {
            responseToClient(
                    response,
                    getErrorResponse(e.getErrorCode(), request.getRequestURL().toString()));
        }
    }

    private ErrorResponse getErrorResponse(BaseErrorCode errorCode, String path) {
        return new ErrorResponse(errorCode.getErrorReason(), path);
    }

    private void responseToClient(HttpServletResponse response, ErrorResponse errorResponse)
            throws IOException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(errorResponse.status());
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
