package com.on.eye.api.global.config.response;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.on.eye.api.global.dto.SuccessResponse;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice(basePackages = "com.on.eye.api")
public class SuccessResponseAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {
        HttpServletRequest servletRequest =
                ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse servletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        logRequest(servletRequest);
        logResponse(servletResponse);

        int status = servletResponse.getStatus();
        HttpStatus resolve = HttpStatus.resolve(status);

        if (resolve == null) return body;

        if (resolve.is2xxSuccessful()) return new SuccessResponse<>(status, body);

        // controller가 String type을 return하는 경우 error 발생 가능성 있음

        return body;
    }

    private void logRequest(HttpServletRequest request) {
        try {
            // HTTP 메소드, URI
            String logMessage = "[Request] " + request.getMethod() + " " + request.getRequestURI();

            log.info(logMessage);

        } catch (Exception e) {
            log.error("Failed to log request", e);
        }
    }

    private void logResponse(HttpServletResponse response) {
        try {
            String logMessage = "[Response] " + response.getStatus();
            log.info(logMessage);

        } catch (Exception e) {
            log.error("Failed to log response", e);
        }
    }
}
