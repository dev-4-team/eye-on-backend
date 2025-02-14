package com.on.eye.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.config.security.AnonymousIdGenerator;
import com.on.eye.api.config.security.SecurityUtils;
import com.on.eye.api.domain.*;
import com.on.eye.api.dto.*;
import com.on.eye.api.exception.DuplicateVerificationException;
import com.on.eye.api.exception.OutOfValidProtestRangeException;
import com.on.eye.api.exception.ProtestNotFoundException;
import com.on.eye.api.mapper.ProtestMapper;
import com.on.eye.api.repository.LocationRepository;
import com.on.eye.api.repository.ParticipantVerificationRepository;
import com.on.eye.api.repository.ProtestRepository;
import com.on.eye.api.repository.ProtestVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtestService {
    private final ProtestRepository protestRepository;
    private final LocationRepository locationRepository;
    private final ParticipantVerificationRepository participantVerificationRepository;
    private final ProtestVerificationRepository protestVerificationRepository;
    private final AnonymousIdGenerator anonymousIdGenerator;

    public List<Long> createProtest(List<ProtestCreateRequest> protestCreateRequests) {
        log.info("시위 {}건 생성 요청", protestCreateRequests.size());

        // 생성 시간 기준으로 상태 자동 설정
        List<ProtestCreateMapping> protestCreateMappings =
                ProtestMapper.toEntity(protestCreateRequests);
        log.debug("시위 정보 매핑 {}건 완료", protestCreateMappings.size());

        // TODO: 뉴시스 크롤링 완료 시, oranizer 세팅도 적용되어야 함
        // set locations using locationDto
        setLocationMappings(protestCreateMappings);
        log.debug("시위 장소 정보 매핑 완료");

        // ProtestLocationMapping도 Casacade 설정으로 함께 저장됨
        List<Protest> protests =
                protestCreateMappings.stream().map(ProtestCreateMapping::getProtest).toList();

        List<ProtestVerification> protestVerifications =
                protests.stream()
                        .map(protest -> ProtestVerification.builder().protest(protest).build())
                        .toList();
        List<Long> response =
                protestRepository.saveAll(protests).stream().map(Protest::getId).toList();
        protestVerificationRepository.saveAll(protestVerifications);

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

        List<ProtestResponse> response = protestRepository.findByStartDateTimeAfterWithOrganizer(startOfDay).stream()
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

    private Protest getProtestById(Long id, boolean isWithOrganizer) {
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
                Location location = getOrCreateLocation(locationDto);

                // Create and add mapping
                createAndAddMapping(protest, location, sequence++);
            }
        }
    }

    private Location getOrCreateLocation(LocationDto locationDto) {
        double similarity = 0.5;
        return locationRepository
                .findMostSimilarLocation(locationDto.name(), similarity)
                .orElseGet(
                        () ->
                                locationRepository.save(
                                        Location.builder()
                                                .name(locationDto.name())
                                                .latitude(locationDto.latitude())
                                                .longitude(locationDto.longitude())
                                                .build()));
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

    private ProtestLocationDto getProtestLocationDto(Protest protest) {
        Long id = protest.getId();
        return locationRepository
                .findFirstLocationByProtestId(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    // TODO: 성능개선 여지 있음
    @Transactional
    public Boolean participateVerify(Long protestId, ParticipateVerificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Protest protest = getProtestById(protestId, false);

        ProtestLocationDto protestLocationDto = getProtestLocationDto(protest);

        Double distance =
                locationRepository.calculateDistance(
                        request.getLatitude(),
                        request.getLongitude(),
                        protestLocationDto.locationId());

        if (distance == null) throw ProtestNotFoundException.EXCEPTION;

        boolean isWithInRadius = distance <= protest.getRadius();

        if (!isWithInRadius) throw OutOfValidProtestRangeException.EXCEPTION;

        try {
            String anonymousUserId =
                    anonymousIdGenerator.generateAnonymousUserId(userId, protestId);
            ParticipantsVerification verification =
                    ParticipantsVerification.builder()
                            .anonymousUserId(anonymousUserId)
                            .protest(protest)
                            .build();
            participantVerificationRepository.save(verification);
            updateProtestVerification(protest);
        } catch (DataIntegrityViolationException e) {
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
}