package com.on.eye.api.protest.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.entity.ProtestStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProtestResponse {
    private final Long id;
    private final String title;
    private final String description;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final String organizer;
    private final Integer declaredParticipants;

    @Enumerated(EnumType.STRING)
    private final ProtestStatus status;

    private final List<LocationDto> locations;
    private final Integer radius;

    @Builder
    public ProtestResponse(
            Long id,
            String title,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String organizer,
            Integer declaredParticipants,
            ProtestStatus status,
            List<LocationDto> locations,
            Integer radius) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.status = status;
        this.locations = locations;
        this.radius = radius;
    }

    public static ProtestResponse from(Protest protest, List<LocationDto> locations) {
        ProtestResponseBuilder builder =
                ProtestResponse.builder()
                        .id(protest.getId())
                        .title(protest.getTitle())
                        .radius(protest.getRadius())
                        .startDateTime(protest.getStartDateTime())
                        .endDateTime(protest.getEndDateTime())
                        .declaredParticipants(protest.getDeclaredParticipants())
                        .status(protest.getStatus())
                        .locations(locations);
        Organizer organizer = protest.getOrganizer();
        if (organizer != null) {
            builder.organizer(organizer.getName())
                    .description(organizer.getDescription())
                    .title(organizer.getTitle());
        }
        return builder.build();
    }
}
