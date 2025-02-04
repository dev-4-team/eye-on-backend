package com.on.eye.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;

public record ProtestResponse(
        @NotNull(message = "id cannot be null") Long id,
        String title,
        String description,
        @NotNull(message = "startDateTime cannot be null") LocalDateTime startDateTime,
        @NotNull(message = "endDateTime cannot be null") LocalDateTime endDateTime,
        String location,
        String organizer,
        @NotNull(message = "declaredParticipants cannot be null") Integer declaredParticipants,
        @Enumerated(EnumType.STRING) ProtestStatus status,
        List<LocationResponse> locations,
        Integer radius) {

    public static ProtestResponse from(Protest protest, List<LocationResponse> locations) {
        return new ProtestResponse(
                protest.getId(),
                protest.getTitle(),
                protest.getDescription(),
                protest.getStartDateTime(),
                protest.getEndDateTime(),
                protest.getLocation(),
                protest.getOrganizer(),
                protest.getDeclaredParticipants(),
                protest.getStatus(),
                locations,
                protest.getRadius());
    }
}
