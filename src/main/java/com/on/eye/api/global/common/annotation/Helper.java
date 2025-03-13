package com.on.eye.api.global.common.annotation;

import java.lang.annotation.*;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component // java bean class 등록(@Component)를 semantic하게(Helper 역할) 해주는 annotation
public @interface Helper {
    @AliasFor(annotation = Component.class)
    String value() default "";
}
