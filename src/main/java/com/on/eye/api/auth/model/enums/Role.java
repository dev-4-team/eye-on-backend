package com.on.eye.api.auth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Role {
    USER("USER"),
    ADMIN("ADMIN");

    private final String value;
}
