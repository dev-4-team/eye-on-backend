package com.on.eye.api.dto;

import com.on.eye.api.domain.Protest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProtestCreateMapping {
    private ProtestCreateRequest protestCreateRequest;
    private Protest protest;
}
