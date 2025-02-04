package com.on.eye.api.mapper;

import java.util.List;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.ProtestCreateMapping;
import com.on.eye.api.dto.ProtestCreateRequest;

public class ProtestMapper {

    private ProtestMapper() {}

    public static List<ProtestCreateMapping> toEntity(
            List<ProtestCreateRequest> protestCreateRequests) {
        return protestCreateRequests.stream()
                .map(
                        protestCreateDto -> {
                            Protest protest =
                                    Protest.builder()
                                            .title(protestCreateDto.title())
                                            .description(protestCreateDto.description())
                                            .location(protestCreateDto.location())
                                            .startDateTime(protestCreateDto.startDateTime())
                                            .endDateTime(protestCreateDto.endDateTime())
                                            .organizer(protestCreateDto.organizer())
                                            .declaredParticipants(
                                                    protestCreateDto.declaredParticipants())
                                            .radius(
                                                    calRadius(
                                                            protestCreateDto
                                                                    .declaredParticipants()))
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
}
