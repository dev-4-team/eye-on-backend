package com.on.eye.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.*;

import com.on.eye.api.validator.ValidProtestDateTimeRange;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ValidProtestDateTimeRange
public class ProtestCreateDto {
    private String title;

    private String description;

    @NotNull(message = "Start date and time cannot be null")
    @Future(message = "Start date and time must be in the future")
    private LocalDateTime startDateTime;

    @NotNull(message = "End date and time cannot be null")
    @Future(message = "End date and time must be in the future")
    private LocalDateTime endDateTime;

    @NotEmpty(message = "Location cannot be empty")
    private String location;

    private String organizer;

    @NotNull(message = "declaredParticipants cannot be null")
    @Min(0)
    @Max(5000000)
    private Integer declaredParticipants;

    @NotEmpty(message = "locations cannot be empty")
    private List<LocationDto> locations;

    @Builder
    public ProtestCreateDto(
            String title,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String location,
            String organizer,
            Integer declaredParticipants,
            List<LocationDto> locations) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.locations = locations;
    }
}
