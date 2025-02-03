package com.on.eye.api.mapper;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestCreateMapping;
import com.on.eye.api.dto.ProtestDetailDto;

import java.util.List;

public class ProtestMapper {

    private ProtestMapper() {
    }

    public static List<ProtestCreateMapping> toEntity(List<ProtestCreateDto> protestCreateDtos) {
        return protestCreateDtos.stream()
                .map(
                        protestCreateDto -> {
                            Protest protest =
                                    Protest.builder()
                                            .title(protestCreateDto.getTitle())
                                            .description(protestCreateDto.getDescription())
                                            .location(protestCreateDto.getLocation())
                                            .startDateTime(protestCreateDto.getStartDateTime())
                                            .endDateTime(protestCreateDto.getEndDateTime())
                                            .organizer(protestCreateDto.getOrganizer())
                                            .declaredParticipants(
                                                    protestCreateDto.getDeclaredParticipants())
                                            .radius(calRadius(protestCreateDto.getDeclaredParticipants()))
                                            .build();
                            return new ProtestCreateMapping(protestCreateDto, protest);
                        })
                .toList();
    }

    private static Integer calRadius(int declaredParticipants) {
        int minRadius = 200;
        int maxRadius = 500;
        int minParticipants = 100;

        Double logParticipants = Math.log10(declaredParticipants) - Math.log10(minParticipants);
        Double logRange = Math.log10(maxRadius) - Math.log10(minParticipants);
        double normalizedLogParticipants = logParticipants / logRange;
        double radius = minRadius + (maxRadius - minRadius) * normalizedLogParticipants;
        return (int) Math.round(radius);
    }

    public static ProtestDetailDto toDto(Protest protest) {
        if (protest == null) return null;
        return ProtestDetailDto.builder()
                .title(protest.getTitle())
                .description(protest.getDescription())
                .location(protest.getLocation())
                .startDateTime(protest.getStartDateTime())
                .endDateTime(protest.getEndDateTime())
                .organizer(protest.getOrganizer())
                .declaredParticipants(protest.getDeclaredParticipants())
                .status(protest.getStatus())
                .build();
    }
}
