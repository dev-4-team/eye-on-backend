package com.on.eye.api.dto;


import com.on.eye.api.validator.ValidProtestDateTimeRange;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


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

    @Getter
    public static class LocationDto {
        @NotEmpty(message = "locationName cannot be empty")
        private final String locationName;

        @NotNull(message = "longitude cannot be null")
        @Digits(integer = 3, fraction = 14, message = "longitude must have up to 3 integer digits and 14 fractional digits")
        BigDecimal latitude;

        @NotNull(message = "latitude cannot be null")
        @Digits(integer = 3, fraction = 14, message = "latitude must have up to 3 integer digits and 14 fractional digits")
        BigDecimal longitude;

        public LocationDto(String locationName, BigDecimal latitude, BigDecimal longitude) {
            this.locationName = locationName;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    @Builder
    public ProtestCreateDto(String title, String description, LocalDateTime startDateTime, LocalDateTime endDateTime,
                            String location, String organizer, Integer declaredParticipants,
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
