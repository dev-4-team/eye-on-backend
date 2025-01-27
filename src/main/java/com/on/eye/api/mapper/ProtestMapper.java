package com.on.eye.api.mapper;

import java.util.List;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestCreateMapping;
import com.on.eye.api.dto.ProtestDetailDto;

public class ProtestMapper {

    private ProtestMapper() {}

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
                                            .build();
                            return new ProtestCreateMapping(protestCreateDto, protest);
                        })
                .toList();
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
