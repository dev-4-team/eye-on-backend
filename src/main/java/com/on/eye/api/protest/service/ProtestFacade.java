package com.on.eye.api.protest.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.cheer.service.CheerSyncService;
import com.on.eye.api.location.entity.ProtestLocationMappings;
import com.on.eye.api.location.service.LocationService;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.service.OrganizerService;
import com.on.eye.api.participant_verification.service.ParticipantVerificationService;
import com.on.eye.api.protest.dto.Coordinate;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest_verification.service.ProtestVerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtestFacade {
    private final ProtestService protestService;
    private final OrganizerService organizerService;
    private final LocationService locationService;
    private final ParticipantVerificationService participantVerificationService;
    private final ProtestVerificationService protestVerificationService;
    private final CheerSyncService cheerSyncService;

    @Transactional
    public List<Long> createProtest(List<ProtestCreateRequest> protestCreateRequests) {
        log.info("시위 {}건 생성 요청", protestCreateRequests.size());

        List<Protest> protests = new ArrayList<>();
        for (ProtestCreateRequest request : protestCreateRequests) {
            Protest protest = Protest.from(request);

            // add locations
            ProtestLocationMappings mappings =
                    locationService.assignLocationMappings(protest, request.locations());
            protest.addLocationMappings(mappings);

            // add organizer
            Organizer organizer = organizerService.getOrCreateOrganizer(request);
            protest.addOrganizer(organizer);

            // add verifications
            protest.addVerification();

            protests.add(protest);
        }

        List<Long> response = protestService.saveAllProtests(protests);

        cheerSyncService.updateTodayCheerCache(response);

        log.info("시위 {}건 생성 완료. 생성된 ID: {}", protests.size(), response);
        return response;
    }

    @Transactional
    public Boolean participateVerify(Long protestId, Coordinate userCoordinate) {
        Protest protest = protestService.getProtestByIdWithLocations(protestId);
        // 유효 반경 내 인증인지 검증
        protest.validateUserCoordinateRange(userCoordinate);

        participantVerificationService.participateVerify(protest, userCoordinate);

        protestVerificationService.updateProtestVerification(protest);

        return true;
    }
}
