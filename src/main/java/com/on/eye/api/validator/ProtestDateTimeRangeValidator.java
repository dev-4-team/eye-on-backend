package com.on.eye.api.validator;

import java.time.Duration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.on.eye.api.dto.ProtestCreateRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProtestDateTimeRangeValidator
        implements ConstraintValidator<ValidProtestDateTimeRange, ProtestCreateRequest> {
    @Override
    public boolean isValid(ProtestCreateRequest createDto, ConstraintValidatorContext context) {

        if (createDto == null
                || createDto.startDateTime() == null
                || createDto.endDateTime() == null) {
            log.warn("시위 생성 데이터 누락");
            return false; // Invalid if any of the fields are null
        }
        if (isInvalidTimePriority(createDto)) {
            log.warn("시위 시간 순서 오류 - 시작:{}, 종료: {}", createDto.startDateTime(), createDto.endDateTime());
            context.buildConstraintViolationWithTemplate(
                            context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("startDateTime")
                    .addPropertyNode("endDateTime")
                    .addConstraintViolation();
            return false;
        }
        return !isInValidTimeDiff(createDto);
    }

    private boolean isInValidTimeDiff(ProtestCreateRequest createDto) {
        long hoursDifference =
                Duration.between(createDto.startDateTime(), createDto.endDateTime()).toHours();

        if (hoursDifference < 1 || hoursDifference > 24) {
            log.warn("시위 시간 범위 오류 - {} 시간", hoursDifference);
            return true;
        }
        return false;
    }

    private boolean isInvalidTimePriority(ProtestCreateRequest createDto) {
        return createDto.endDateTime().isBefore(createDto.startDateTime());
    }
}
