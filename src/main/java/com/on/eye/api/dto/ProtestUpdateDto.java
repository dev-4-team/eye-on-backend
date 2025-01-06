package com.on.eye.api.dto;

import com.on.eye.api.domain.ProtestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ProtestUpdateDto {
    private final String title;
    private final String description;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final String location;
    private final String organizer;
    private final Integer declaredParticipants;

    @Enumerated(EnumType.STRING)
    private final ProtestStatus status;

    @Builder
    public ProtestUpdateDto(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime, String location, String organizer, Integer declaredParticipants, ProtestStatus status) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.status = status;
    }
}
