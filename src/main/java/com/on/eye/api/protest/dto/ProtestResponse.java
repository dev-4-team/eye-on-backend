package com.on.eye.api.protest.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.organizer.dto.OrganizerResponse;
import com.on.eye.api.protest.entity.Protest;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ProtestResponse {
    private final Long id;
    private String title;
    private String description;
    private String organizer;
    private final LocalDateTime startDateTime;
    private final LocalDateTime endDateTime;
    private final Integer declaredParticipants;

    private final List<LocationDto> locations;
    private final Integer radius;

    @Builder
    public ProtestResponse(
            Long id,
            String title,
            OrganizerResponse organizerResponse,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            Integer declaredParticipants,
            List<LocationDto> locations,
            Integer radius) {
        this.id = id;
        this.title = title;
        if (organizerResponse != null) {
            this.title = organizerResponse.title();
            this.description = organizerResponse.description();
            this.organizer = organizerResponse.name();
        }
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.declaredParticipants = declaredParticipants;
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
                        .locations(locations);
        OrganizerResponse organizer = protest.getOrganizer().toResponse();
        if (organizer != null) {
            builder.organizerResponse(organizer);
        }
        return builder.build();
    }
}
