package com.on.eye.api.auth.error.exception;

import static com.on.eye.api.global.constants.ErrorStatus.*;

import java.lang.reflect.Field;
import java.util.Objects;

import com.on.eye.api.global.common.annotation.ExplainError;
import com.on.eye.api.global.error.exception.BaseErrorCode;
import com.on.eye.api.global.error.exception.ErrorReason;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {
    @ExplainError("회원가입 시 이미 존재하는 유저일 시 발생. 회원가입 전 항상 register valid check 필요")
    USER_ALREADY_EXIST(BAD_REQUEST, "USER_400_1", "이미 회원 가입한 유저입니다"),

    @ExplainError("탈퇴한 유저에 대해서는 접근할 수 없음")
    USER_ALREADY_DELETED(FORBIDDEN, "USER_403_1", "이미 삭제된 유저입니다"),

    @ExplainError("유저 정보를 못찾음")
    USER_NOT_FOUND(NOT_FOUND, "USER_404_1", "유저 정보를 찾을 수 없습니다");

    private final Integer status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.builder().status(status).code(code).message(message).build();
    }

    @Override
    public String getExplainError() throws NoSuchFieldException {
        Field field = this.getClass().getField(this.name());
        ExplainError annotation = field.getAnnotation(ExplainError.class);
        return Objects.nonNull(annotation) ? annotation.value() : this.getMessage();
    }
}
