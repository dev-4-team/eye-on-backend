package com.on.eye.api.location.error.exception;

import static com.on.eye.api.global.constants.ErrorStatus.NOT_FOUND;

import java.lang.reflect.Field;
import java.util.Objects;

import com.on.eye.api.global.common.annotation.ExplainError;
import com.on.eye.api.global.error.exception.BaseErrorCode;
import com.on.eye.api.global.error.exception.ErrorReason;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LocationErrorCode implements BaseErrorCode {
    LOCATION_NOT_FOUND(NOT_FOUND, "LOCATION_400_1", "해당 id의 위치 정보를 조회할 수 없습니다");

    private final Integer status;
    private final String code;
    private final String message;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.builder().message(message).code(code).status(status).build();
    }

    @Override
    public String getExplainError() throws NoSuchFieldException {
        Field field = this.getClass().getField(this.name());
        ExplainError annotation = field.getAnnotation(ExplainError.class);
        return Objects.nonNull(annotation) ? annotation.value() : this.getMessage();
    }
}
