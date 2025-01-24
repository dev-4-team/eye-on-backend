package com.on.eye.api.auth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthProvider {
    KAKAO("KAKAO");

    private final String value;
}
