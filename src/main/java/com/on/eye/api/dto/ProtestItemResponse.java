package com.on.eye.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;

public record ProtestItemResponse(
        Long id,
        String title,
        String description,
        Integer radius,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String location,
        String organizer,
        Integer declaredParticipants,
        @Enumerated(EnumType.STRING) ProtestStatus status,
        List<LocationResponse> locations) {

    public static ProtestItemResponse from(Protest protest, List<LocationResponse> locations) {
        return new ProtestItemResponse(
                protest.getId(),
                protest.getTitle(),
                protest.getDescription(),
                protest.getRadius(),
                protest.getStartDateTime(),
                protest.getEndDateTime(),
                protest.getLocation(),
                protest.getOrganizer(),
                protest.getDeclaredParticipants(),
                protest.getStatus(),
                locations);
    }
}
