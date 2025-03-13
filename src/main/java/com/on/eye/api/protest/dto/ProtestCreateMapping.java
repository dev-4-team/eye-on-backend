package com.on.eye.api.protest.dto;

import com.on.eye.api.protest.entity.Protest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProtestCreateMapping {
    private ProtestCreateRequest protestCreateRequest;
    private Protest protest;
}
