package com.on.eye.api.common.annotation;

import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

public class CollectionValidator implements Validator {
    private final Validator validator;

    public CollectionValidator(LocalValidatorFactoryBean validator) {
        this.validator = validator;
    }

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return List.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        List<?> targetList = (List<?>) target;
        for (Object item : targetList) {
            ValidationUtils.invokeValidator(validator, item, errors);
        }
    }
}
