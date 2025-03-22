package com.on.eye.api.cheer.dto;

import java.time.LocalDateTime;

public record CheerUpdateDto(Long protestId, Integer cheerCount, LocalDateTime timestamp) {
    public static CheerUpdateDto of(Long protestId, Integer cheerCount) {
        return new CheerUpdateDto(protestId, cheerCount, LocalDateTime.now());
    }
}
