package com.on.eye.api.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.on.eye.api.dto.ErrorResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            @NonNull HttpHeaders headers,
            HttpStatusCode statusCode,
            @NonNull WebRequest request) {
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        String url =
                ServletUriComponentsBuilder.fromRequest(servletWebRequest.getRequest())
                        .build()
                        .toUriString();

        ErrorResponse errorResponse =
                new ErrorResponse(statusCode.value(), statusCode.toString(), ex.getMessage(), url);

        log.error(
                "내부 오류 발생 - Status: {}, URL: {}, 메시지: {}",
                statusCode.value(),
                url,
                ex.getMessage());
        return super.handleExceptionInternal(ex, errorResponse, headers, statusCode, request);
    }

    @Override
    @SneakyThrows
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            HttpStatusCode status,
            @NonNull WebRequest request) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        String url =
                ServletUriComponentsBuilder.fromRequest(servletWebRequest.getRequest())
                        .build()
                        .toUriString();
        Map<String, Object> fieldAndErrorMessages =
                fieldErrors.stream()
                        .collect(
                                Collectors.toMap(
                                        FieldError::getField,
                                        error ->
                                                error.getDefaultMessage() != null
                                                        ? error.getDefaultMessage()
                                                        : "No message available",
                                        (existing, replacement) -> existing));

        String errorsToJsonString = objectMapper.writeValueAsString(fieldAndErrorMessages);
        ErrorResponse errorResponse =
                new ErrorResponse(status.value(), status.toString(), errorsToJsonString, url);

        log.warn("요청 데이터의 검증 실패 - URL: {}, 오류: {}", url, errorsToJsonString);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(CustomCodeException.class)
    public ResponseEntity<ErrorResponse> handleCustomCodeException(
            CustomCodeException e, HttpServletRequest request) {
        BaseErrorCode errorCode = e.getErrorCode();
        ErrorReason errorReason = errorCode.getErrorReason();
        return ResponseEntity.status(HttpStatus.valueOf(errorReason.getStatus()))
                .body(new ErrorResponse(errorReason, request.getRequestURL().toString()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e, HttpServletRequest request) {
        Map<String, Object> bindingErrors = new HashMap<>();

        e.getConstraintViolations()
                .forEach(
                        constraintViolation -> {
                            List<String> propertyPath =
                                    List.of(
                                            constraintViolation
                                                    .getPropertyPath()
                                                    .toString()
                                                    .split("\\."));
                            String path =
                                    propertyPath.stream()
                                            .skip(propertyPath.size() - 1L)
                                            .findFirst()
                                            .orElse(null);
                            bindingErrors.put(path, constraintViolation.getMessage());
                        });

        ErrorReason errorReason =
                ErrorReason.builder()
                        .code("BAD_REQUEST")
                        .status(HttpStatus.BAD_REQUEST.value())
                        .message(bindingErrors.toString())
                        .build();
        ErrorResponse errorResponse =
                new ErrorResponse(errorReason, request.getRequestURL().toString());

        log.warn("제약 조건 위반 - URL: {}, 오류: {}", request.getRequestURL(), bindingErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
