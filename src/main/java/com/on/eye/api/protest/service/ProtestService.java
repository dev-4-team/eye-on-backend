package com.on.eye.api.protest.service;

import com.on.eye.api.auth.error.exception.OutOfValidProtestRangeException;
import com.on.eye.api.cheer.service.CheerSyncService;
import com.on.eye.api.global.config.security.AnonymousIdGenerator;
import com.on.eye.api.global.config.security.SecurityUtils;
import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.dto.ProtestLocationDto;
import com.on.eye.api.location.entity.ProtestLocationMappings;
import com.on.eye.api.location.service.LocationService;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.service.OrganizerService;
import com.on.eye.api.protest.dto.*;
import com.on.eye.api.protest.entity.ParticipantsVerification;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.entity.ProtestVerification;
import com.on.eye.api.protest.error.exception.AbnormalMovementPatternException;
import com.on.eye.api.protest.error.exception.DuplicateVerificationException;
import com.on.eye.api.protest.error.exception.ProtestNotFoundException;
import com.on.eye.api.protest.repository.ParticipantVerificationRepository;
import com.on.eye.api.protest.repository.ProtestRepository;
import com.on.eye.api.protest.repository.ProtestVerificationRepository;
import com.on.eye.api.protest.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.on.eye.api.protest.util.GeoUtils.haversineDistance;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtestService {
    private final ProtestRepository protestRepository;
    private final ParticipantVerificationRepository participantVerificationRepository;
    private final ProtestVerificationRepository protestVerificationRepository;
    private final AnonymousIdGenerator anonymousIdGenerator;

    private final OrganizerService organizerService;
    private final LocationService locationService;
    private final CheerSyncService cheerSyncService;

    @Transactional
    public List<Long> createProtest(List<ProtestCreateRequest> protestCreateRequests) {
        log.info("시위 {}건 생성 요청", protestCreateRequests.size());

        List<Protest> protests = new ArrayList<>();
        for (ProtestCreateRequest request : protestCreateRequests) {
            Protest protest = Protest.from(request);

            // add locations
            ProtestLocationMappings mappings = locationService.assignLocationMappings(protest, request.locations());
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
        log.info("시위 상세 정보 조회 요청 - ID: {}", id);

        Protest protest = getProtestById(id, true);
        log.debug("시위 기본 정보 조회 완료 - 제목: {}", protest.getTitle());

        List<LocationDto> locations = protest.getLocationMappings().toLocationDtos();
        log.debug("시위 장소 정보 조회 완료 - 총 {}개", locations.size());

        log.info("시위 상세 정보 조회 완료 - ID: {}", id);
        return ProtestResponse.from(protest, locations);
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
                        .map(
                                protest -> {
                                    List<LocationDto> locations =
                                            protest.getLocationMappings().toLocationDtos();
                                    return ProtestResponse.from(protest, locations);
                                })
                        .toList();

        log.info("날짜 별 시위 조회 완료 - 날짜: {}, 조회된 시위: {}건", date, response.size());
        return response;
    }

    public Protest getProtestById(Long id, boolean isWithOrganizer) {
        if (isWithOrganizer)
            return protestRepository
                    .findByProtestIdWithOrganizer(id)
                    .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
        return protestRepository.findById(id).orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    @Transactional
    public Boolean participateVerify(Long protestId, ParticipateVerificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Protest protest = getProtestById(protestId, false);
        // TODO: ProtestLocationMappings or Locations 등에서 해결 가능
        ProtestLocationDto protestLocationDto = locationService.getProtestCenterLocation(protest);

        // 유효 반경 내 인증인지 검증
        double distance =
                haversineDistance(
                        protestLocationDto.latitude(),
                        protestLocationDto.longitude(),
                        request.latitude(),
                        request.longitude());
        boolean isWithInRadius = distance <= protest.getRadius();
        if (!isWithInRadius) throw OutOfValidProtestRangeException.EXCEPTION;

        try {
            String anonymousUserId = anonymousIdGenerator.generateAnonymousUserId(userId);

            participantVerificationRepository
                    .findMostRecentVerificationByUserSince(
                            LocalDateTime.now().withHour(0), anonymousUserId)
                    .ifPresent(
                            recentVerifications ->
                                    detectAbnormalMovementPattern(request, recentVerifications));

            ParticipantsVerification verification =
                    ParticipantsVerification.builder()
                            .anonymousUserId(anonymousUserId)
                            .protest(protest)
                            .build();
            participantVerificationRepository.save(verification);
            updateProtestVerification(protest);
        } catch (DataIntegrityViolationException e) {
            // 중복 인증 검증
            throw DuplicateVerificationException.EXCEPTION;
        }

        return true;
    }

    private void updateProtestVerification(Protest protest) {
        protestVerificationRepository.increaseVerifiedNum(protest.getId());
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
        Protest protest = getProtestById(protestId, false);

        ProtestVerification protestVerification =
                protestVerificationRepository.findByProtestId(protest.getId());
        return List.of(ProtestVerificationResponse.from(protestVerification));
    }

    private void detectAbnormalMovementPattern(
            ParticipateVerificationRequest verificationRequest,
            VerificationHistory oldVerificationHistory) {
        final double MAX_SPEED_MPS = 16; // 약 시속 60km

        double distanceDiff =
                GeoUtils.haversineDistance(
                        verificationRequest.latitude(),
                        verificationRequest.longitude(),
                        oldVerificationHistory.latitude(),
                        oldVerificationHistory.longitude());
        long timeDiff =
                Duration.between(oldVerificationHistory.verifiedAt(), LocalDateTime.now())
                        .getSeconds();
        if (timeDiff < 1) timeDiff = 1;

        double speedInMeterPerSec = distanceDiff / timeDiff;

        if (speedInMeterPerSec > MAX_SPEED_MPS) {
            throw AbnormalMovementPatternException.EXCEPTION;
        }
    }
}
