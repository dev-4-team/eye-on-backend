package com.on.eye.api.validator;

import java.time.Duration;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import com.on.eye.api.dto.ProtestCreateRequest;

public class ProtestDateTimeRangeValidator
        implements ConstraintValidator<ValidProtestDateTimeRange, ProtestCreateRequest> {
    @Override
    public boolean isValid(ProtestCreateRequest createDto, ConstraintValidatorContext context) {

        if (createDto == null
                || createDto.startDateTime() == null
                || createDto.endDateTime() == null) {
            return false; // Invalid if any of the fields are null
        }
        if (isInvalidTimePriority(createDto)) {
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
        return hoursDifference < 1 || hoursDifference > 24;
    }

    private boolean isInvalidTimePriority(ProtestCreateRequest createDto) {
        return createDto.endDateTime().isBefore(createDto.startDateTime());
    }
}
