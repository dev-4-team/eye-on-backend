package com.on.eye.api.dto;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProtestListItemDto {
    private final Long id;
    private final String title;

    @NotNull(message = "startDateTime cannot be null")
    private final LocalDateTime startDateTime;

    @NotNull(message = "endDateTime cannot be null")
    private final LocalDateTime endDateTime;
    private final String location;
    private final String organizer;

    @NotNull(message = "declaredParticipants cannot be null")
    private final Integer declaredParticipants;

    @Enumerated(EnumType.STRING)
    private ProtestStatus status;

    public ProtestListItemDto(Long id, String title, LocalDateTime startDateTime, LocalDateTime endDateTime, String location, String organizer, Integer declaredParticipants, ProtestStatus status) {
        this.id = id;
        this.title = title;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.status = status;
    }

    public static ProtestListItemDto from(Protest protest) {
        return ProtestListItemDto.builder()
                .id(protest.getId())
                .title(protest.getTitle())
                .startDateTime(protest.getStartDateTime())
                .endDateTime(protest.getEndDateTime())
                .location(protest.getLocation())
                .organizer(protest.getOrganizer())
                .declaredParticipants(protest.getDeclaredParticipants())
                .status(protest.getStatus())
                .build();
    }

}
