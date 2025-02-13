package com.on.eye.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.*;

import com.on.eye.api.validator.ValidProtestDateTimeRange;

@ValidProtestDateTimeRange
public record ProtestCreateRequest(
        String title,
        @NotNull(message = "Start date and time cannot be null")
                @Future(message = "Start date and time must be in the future")
                LocalDateTime startDateTime,
        @NotNull(message = "End date and time cannot be null")
                @Future(message = "End date and time must be in the future")
                LocalDateTime endDateTime,
        String organizer,
        @NotNull(message = "declaredParticipants cannot be null") @Min(0) @Max(5000000)
                Integer declaredParticipants,
        @NotEmpty(message = "locations cannot be empty") List<LocationDto> locations) {}
