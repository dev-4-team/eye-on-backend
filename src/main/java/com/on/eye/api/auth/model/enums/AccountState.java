package com.on.eye.api.auth.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AccountState {
    NORMAL("NORMAL"),
    DELETED("DELETED"),
    BLOCKED("BLOCKED");

    private final String value;
}
