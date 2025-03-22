package com.on.eye.api.protest.service;

import static com.on.eye.api.protest.util.GeoUtils.haversineDistance;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.auth.error.exception.OutOfValidProtestRangeException;
import com.on.eye.api.cheer.service.CheerSyncService;
import com.on.eye.api.global.config.security.AnonymousIdGenerator;
import com.on.eye.api.global.config.security.SecurityUtils;
import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.dto.ProtestLocationDto;
import com.on.eye.api.location.entity.Location;
import com.on.eye.api.location.entity.ProtestLocationMapping;
import com.on.eye.api.location.service.LocationService;
import com.on.eye.api.organizer.service.OrganizerService;
import com.on.eye.api.protest.dto.*;
import com.on.eye.api.protest.entity.ParticipantsVerification;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.entity.ProtestVerification;
import com.on.eye.api.protest.error.exception.AbnormalMovementPatternException;
import com.on.eye.api.protest.error.exception.DuplicateVerificationException;
import com.on.eye.api.protest.error.exception.ProtestNotFoundException;
import com.on.eye.api.protest.mapper.ProtestMapper;
import com.on.eye.api.protest.repository.ParticipantVerificationRepository;
import com.on.eye.api.protest.repository.ProtestRepository;
import com.on.eye.api.protest.repository.ProtestVerificationRepository;
import com.on.eye.api.protest.util.GeoUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        // 생성 시간 기준으로 상태 자동 설정
        List<ProtestCreateMapping> protestCreateMappings =
                ProtestMapper.toEntity(protestCreateRequests);
        log.debug("시위 정보 매핑 {}건 완료", protestCreateMappings.size());

        // set locations using locationDto
        setLocationMappings(protestCreateMappings);
        log.debug("시위 장소 정보 매핑 완료");

        organizerService.checkOrganizer(protestCreateMappings);

        // ProtestLocationMapping도 Casacade 설정으로 함께 저장됨
        List<Protest> protests =
                protestCreateMappings.stream()
                        .map(
                                mapping -> {
                                    Protest protest = mapping.getProtest();
                                    ProtestVerification protestVerification =
                                            new ProtestVerification(protest);
                                    protest.setProtestVerification(protestVerification);
                                    return protest;
                                })
                        .toList();

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

        List<LocationDto> locations = getLocations(protest);
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
                                    List<LocationDto> locations = getLocations(protest);
                                    return ProtestResponse.from(protest, locations);
                                })
                        .toList();

        log.info("날짜 별 시위 조회 완료 - 날짜: {}, 조회된 시위: {}건", date, response.size());
        return response;
    }

    public Long updateProtest(Long id, ProtestUpdateDto updateDto) {
        log.info("시위 정보 수정 요청 - ID: {}", id);

        // Find the protest by ID
        Protest protest = getProtestById(id, true);
        log.debug("시위 정보 조회 완료 - 제목: {}", protest.getTitle());

        // Reflect non-null updateDto fields into the protest entity
        applyUpdates(protest, updateDto);
        log.debug("시위 정보 변경 적용 - 제목: {}", protest.getTitle());

        // Save the updated entity back to the database
        Protest updatedProtest = protestRepository.save(protest);

        log.info("시위 정보 수정 완료 - ID: {}", updatedProtest.getId());
        // Return the ID of the updated protest
        return updatedProtest.getId();
    }

    private void applyUpdates(Protest protest, ProtestUpdateDto updateDto) {
        if (updateDto != null) {
            Optional.ofNullable(updateDto.getTitle()).ifPresent(protest::setTitle);
            Optional.ofNullable(updateDto.getDescription()).ifPresent(protest::setDescription);
            Optional.ofNullable(updateDto.getStartDateTime()).ifPresent(protest::setStartDateTime);
            Optional.ofNullable(updateDto.getEndDateTime()).ifPresent(protest::setEndDateTime);
            Optional.ofNullable(updateDto.getDeclaredParticipants())
                    .ifPresent(protest::setDeclaredParticipants);
            Optional.ofNullable(updateDto.getStatus()).ifPresent(protest::setStatus);
        }
    }

    public Protest getProtestById(Long id, boolean isWithOrganizer) {
        if (isWithOrganizer)
            return protestRepository
                    .findByProtestIdWithOrganizer(id)
                    .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
        return protestRepository.findById(id).orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    private void setLocationMappings(List<ProtestCreateMapping> protestCreateMappings) {
        int sequence = 0;
        for (ProtestCreateMapping mapping : protestCreateMappings) {
            Protest protest = mapping.getProtest();
            ProtestCreateRequest protestCreateRequest = mapping.getProtestCreateRequest();
            for (LocationDto locationDto : protestCreateRequest.locations()) {
                // Retrieve or create location
                Location location = locationService.getOrCreateLocation(locationDto);

                // Create and add mapping
                createAndAddMapping(protest, location, sequence++);
            }
        }
    }

    private void createAndAddMapping(Protest protest, Location location, int sequence) {
        ProtestLocationMapping mapping =
                ProtestLocationMapping.builder()
                        .protest(protest)
                        .location(location)
                        .sequence(sequence)
                        .build();

        protest.getLocationMappings().add(mapping);
    }

    private List<LocationDto> getLocations(Protest protest) {
        return protest.getLocationMappings().stream()
                .sorted(Comparator.comparing(ProtestLocationMapping::getSequence))
                .map(mapping -> LocationDto.from(mapping.getLocation()))
                .toList();
    }

    @Transactional
    public Boolean participateVerify(Long protestId, ParticipateVerificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Protest protest = getProtestById(protestId, false);
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
