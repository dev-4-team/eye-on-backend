package com.on.eye.api.dto;

import com.on.eye.api.validator.ValidProtestDateTimeRange;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;

@ValidProtestDateTimeRange
public record ProtestCreateRequest(
        String title,
        @NotNull(message = "Start date and time cannot be null")
        LocalDateTime startDateTime,
        @NotNull(message = "End date and time cannot be null")
        LocalDateTime endDateTime,
        String organizer,
        @NotNull(message = "declaredParticipants cannot be null") @Min(0) @Max(5000000)
        Integer declaredParticipants,
        @NotEmpty(message = "locations cannot be empty") List<LocationDto> locations) {
}
