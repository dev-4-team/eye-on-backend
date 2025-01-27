package com.on.eye.api.dto;

import com.on.eye.api.domain.Protest;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProtestCreateMapping {
    private ProtestCreateDto protestCreateDto;
    private Protest protest;
}
