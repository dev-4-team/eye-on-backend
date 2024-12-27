package com.on.eye.api.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ProtestDateTimeRangeValidator.class)
public @interface ValidProtestDateTimeRange {
    String message() default "StartDateTime must be prior to EndDateTime And the difference between them should be more than 1 hour, less than 24 hours";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
