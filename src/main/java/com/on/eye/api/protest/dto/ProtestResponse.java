package com.on.eye.api.protest.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.organizer.dto.OrganizerResponse;
import com.on.eye.api.protest.entity.Protest;

import lombok.Builder;

@Builder
public record ProtestResponse(
        Long id,
        String title,
        String description,
        String organizer,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        Integer declaredParticipants,
        List<LocationDto> locations,
        Integer radius) {
    // 정적 팩토리 메서드에서 빌더 사용
    public static ProtestResponse from(Protest protest, List<LocationDto> locations) {
        OrganizerResponse organizer = protest.getOrganizer().toResponse();

        return ProtestResponse.builder()
                .id(protest.getId())
                .title(protest.getTitle())
                .description(organizer != null ? organizer.description() : null)
                .organizer(organizer != null ? organizer.name() : null)
                .startDateTime(protest.getStartDateTime())
                .endDateTime(protest.getEndDateTime())
                .declaredParticipants(protest.getDeclaredParticipants())
                .locations(locations)
                .radius(protest.getRadius())
                .build();
    }
}
