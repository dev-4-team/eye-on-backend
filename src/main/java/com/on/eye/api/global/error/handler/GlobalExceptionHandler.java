package com.on.eye.api.global.error.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.BadSqlGrammarException;
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
import com.on.eye.api.global.dto.ErrorResponse;
import com.on.eye.api.global.error.exception.BaseErrorCode;
import com.on.eye.api.global.error.exception.CustomCodeException;
import com.on.eye.api.global.error.exception.ErrorReason;

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

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(
            DataAccessException ex, HttpServletRequest request) {

        log.error("데이터베이스 액세스 예외 발생 - URL: {}, 예외: {} ", request.getRequestURL(), ex);

        // 예외 타입에 따라 다른 메시지와 상태 코드 반환
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "데이터베이스 작업 중 오류가 발생했습니다.";

        // 특정 예외 유형에 따라 처리 분기
        if (ex instanceof DataIntegrityViolationException) {
            status = HttpStatus.BAD_REQUEST;
            message = "데이터 무결성 제약 조건을 위반했습니다.";
        } else if (ex instanceof QueryTimeoutException) {
            message = "데이터베이스 쿼리 시간이 초과되었습니다.";
        } else if (ex instanceof BadSqlGrammarException) {
            message = "잘못된 SQL 쿼리가 실행되었습니다.";
            // 이 정보는 내부 개발 문제이므로 500 유지
        }

        ErrorReason errorReason =
                ErrorReason.builder()
                        .status(status.value())
                        .code("DATABASE_EXCEPTION")
                        .message(message)
                        .build();

        ErrorResponse errorResponse =
                new ErrorResponse(errorReason, request.getRequestURL().toString());

        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex, HttpServletRequest request) {
        log.error("처리되지 않은 예외 발생 - URL: {}, 예외: {} ", request.getRequestURL(), ex);

        ErrorReason errorReason =
                ErrorReason.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .code("UNDEFINED_INTERNAL_SERVER_ERROR")
                        .message(ex.getMessage())
                        .build();

        ErrorResponse errorResponse =
                new ErrorResponse(errorReason, request.getRequestURL().toString());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
