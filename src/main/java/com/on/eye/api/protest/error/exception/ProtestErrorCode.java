package com.on.eye.api.protest.error.exception;

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
public enum ProtestErrorCode implements BaseErrorCode {
    PROTEST_NOT_FOUND(NOT_FOUND, "PROTEST_400_1", "해당 id의 시위 정보를 조회할 수 없습니다"),
    PROTEST_CACHE_NOT_FOUND(NOT_FOUND, "PROTEST_400_2", "Cache에서 해당 id의 시위 정보를 조회할 수 없습니다"),
    DUPLICATED_VERIFICATION(CONFLICT, "PROTEST_409_1", "한개의 시위에 중복 인증은 불가능합니다"),
    OUT_OF_VALID_PROTEST_RANGE(UNPROCESSABLE_ENTITY, "PROTEST_422_1", "유효한 시위 참여인증 범위 밖에 있습니다"),
    ABNORMAL_MOVEMENT_PATTERN(UNPROCESSABLE_ENTITY, "PROTEST_422_2", "불가능한 이동속도 인증입니다");

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
