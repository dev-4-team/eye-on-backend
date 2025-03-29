package com.on.eye.api.protest.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import com.on.eye.api.protest.dto.ProtestResponse;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.error.exception.ProtestNotFoundException;
import com.on.eye.api.protest.repository.ProtestRepository;
import com.on.eye.api.protest_verification.dto.ProtestVerificationResponse;
import com.on.eye.api.protest_verification.entity.ProtestVerification;
import com.on.eye.api.protest_verification.repository.ProtestVerificationRepository;
import com.on.eye.api.protest_verification.service.ProtestVerificationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtestService {
    private final ProtestRepository protestRepository;
    private final ProtestVerificationRepository protestVerificationRepository;

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

        List<Long> response =
                protestRepository.saveAll(protests).stream().map(Protest::getId).toList();

        cheerSyncService.updateTodayCheerCache(response);

        log.info("시위 {}건 생성 완료. 생성된 ID: {}", protests.size(), response);
        return response;
    }

    @Transactional(readOnly = true)
    public ProtestResponse getProtestDetail(Long id) {
        Protest protest = getProtestByIdWithOrganizer(id);

        return protest.toResponse();
    }

    @Transactional(readOnly = true)
    public List<ProtestResponse> getProtestsBy(LocalDate date) {
        log.info("날짜 별 시위 조회 요청 - 날짜: {}", date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        List<ProtestResponse> response =
                protestRepository
                        .findByStartDateTimeAfterWithOrganizer(startOfDay, endOfDay)
                        .stream()
                        .map(Protest::toResponse)
                        .toList();

        log.info("날짜 별 시위 조회 완료 - 날짜: {}, 조회된 시위: {}건", date, response.size());
        return response;
    }

    @Transactional
    public Boolean participateVerify(Long protestId, Coordinate userCoordinate) {
        Protest protest = getProtestByIdWithLocations(protestId);
        // 유효 반경 내 인증인지 검증
        protest.validateUserCoordinateRange(userCoordinate);

        participantVerificationService.participateVerify(protest, userCoordinate);

        protestVerificationService.updateProtestVerification(protest);

        return true;
    }

    public List<ProtestVerificationResponse> getProtestVerifications(
            Long protestId, LocalDate date) {
        if (protestId == null) {
            return protestRepository.findByStartDateTimeAfter(date.atStartOfDay()).stream()
                    .map(
                            protest -> {
                                ProtestVerification protestVerification =
                                        protestVerificationRepository.findByProtestId(
                                                protest.getId());
                                return protestVerification == null
                                        ? null
                                        : ProtestVerificationResponse.from(protestVerification);
                            })
                    .toList();
        }
        Protest protest = getProtestById(protestId);

        ProtestVerification protestVerification =
                protestVerificationRepository.findByProtestId(protest.getId());
        return List.of(ProtestVerificationResponse.from(protestVerification));
    }

    private Protest getProtestById(Long id) {
        return protestRepository.findById(id).orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    private Protest getProtestByIdWithOrganizer(Long id) {
        return protestRepository
                .findByProtestIdWithOrganizer(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    private Protest getProtestByIdWithLocations(Long id) {
        return protestRepository
                .findByProtestIdWithLocations(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }
}
