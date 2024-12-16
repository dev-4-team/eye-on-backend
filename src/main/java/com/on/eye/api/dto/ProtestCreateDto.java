package com.on.eye.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ProtestCreateDto {
    private String title;

    private String description;

    @NotNull(message = "Start date and time cannot be null")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time cannot be null")
    private LocalDateTime endDateTime;

    @NotEmpty(message = "Location cannot be empty")
    private String location;

    private String organizer;

    @NotEmpty(message = "declaredParticipants cannot be empty")
    private Integer declaredParticipants;

    public boolean isValidDateTimeRange() {
        return this.startDateTime != null && this.endDateTime != null && this.startDateTime.isBefore(this.endDateTime);
    }

    @Builder
    public ProtestCreateDto(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime, String location, String organizer, Integer declaredParticipants) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
    }
}
