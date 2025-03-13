package com.on.eye.api.protest.mapper;

import java.util.List;

import com.on.eye.api.protest.dto.ProtestCreateMapping;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import com.on.eye.api.protest.entity.Protest;

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
                                            .startDateTime(protestCreateDto.startDateTime())
                                            .endDateTime(protestCreateDto.endDateTime())
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
        // 최소/최대 제한 설정
        if (declaredParticipants < 10) return 10;
        if (declaredParticipants > 500000) return 500;

        // 로그 스케일 변환을 위한 기본값 설정
        final double MIN_PARTICIPANTS = Math.log(10);
        final double MAX_PARTICIPANTS = Math.log(500000);
        final double MIN_RADIUS = 10;
        final double MAX_RADIUS = 500;

        // 로그 스케일 변환 후 선형 매핑
        double logParticipants = Math.log(declaredParticipants);
        double radius =
                MIN_RADIUS
                        + (logParticipants - MIN_PARTICIPANTS)
                                * (MAX_RADIUS - MIN_RADIUS)
                                / (MAX_PARTICIPANTS - MIN_PARTICIPANTS);

        // 정수로 반올림
        return (int) Math.round(radius);
    }
}
