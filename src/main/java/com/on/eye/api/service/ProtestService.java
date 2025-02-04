package com.on.eye.api.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.auth.model.entity.User;
import com.on.eye.api.auth.repository.UserRepository;
import com.on.eye.api.config.security.SecurityUtils;
import com.on.eye.api.domain.Location;
import com.on.eye.api.domain.ParticipantsVerification;
import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestLocationMapping;
import com.on.eye.api.dto.*;
import com.on.eye.api.exception.DuplicateVerificationException;
import com.on.eye.api.exception.OutOfValidProtestRangeException;
import com.on.eye.api.exception.ProtestNotFoundException;
import com.on.eye.api.mapper.ProtestMapper;
import com.on.eye.api.repository.LocationRepository;
import com.on.eye.api.repository.ParticipantVerificationRepository;
import com.on.eye.api.repository.ProtestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProtestService {
    private final ProtestRepository protestRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ParticipantVerificationRepository participantVerificationRepository;

    public List<Protest> createProtest(List<ProtestCreateRequest> protestCreateRequests) {
        // 생성 시간 기준으로 상태 자동 설정
        List<ProtestCreateMapping> protestCreateMappings =
                ProtestMapper.toEntity(protestCreateRequests);

        // set locations using locationDto
        setLocationMappings(protestCreateMappings);
        // ProtestLocationMapping도 Casacade 설정으로 함께 저장됨
        List<Protest> protests =
                protestCreateMappings.stream().map(ProtestCreateMapping::getProtest).toList();
        return protestRepository.saveAll(protests);
    }

    @Transactional(readOnly = true)
    public ProtestResponse getProtestDetail(Long id) {
        Protest protest = getProtestById(id);

        List<LocationResponse> locations = getLocations(protest);

        return ProtestResponse.from(protest, locations);
    }

    @Transactional(readOnly = true)
    public List<ProtestItemResponse> getProtestsBy(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();

        return protestRepository.findByStartDateTimeAfter(startOfDay).stream()
                .map(
                        protest -> {
                            List<LocationResponse> locations = getLocations(protest);
                            return ProtestItemResponse.from(protest, locations);
                        })
                .toList();
    }

    public Long updateProtest(Long id, ProtestUpdateDto updateDto) {
        // Find the protest by ID
        Protest protest = getProtestById(id);

        // Reflect non-null updateDto fields into the protest entity
        applyUpdates(protest, updateDto);

        // Save the updated entity back to the database
        Protest updatedProtest = protestRepository.save(protest);

        // Return the ID of the updated protest
        return updatedProtest.getId();
    }

    private void applyUpdates(Protest protest, ProtestUpdateDto updateDto) {
        if (updateDto != null) {
            Optional.ofNullable(updateDto.getTitle()).ifPresent(protest::setTitle);
            Optional.ofNullable(updateDto.getDescription()).ifPresent(protest::setDescription);
            Optional.ofNullable(updateDto.getStartDateTime()).ifPresent(protest::setStartDateTime);
            Optional.ofNullable(updateDto.getEndDateTime()).ifPresent(protest::setEndDateTime);
            Optional.ofNullable(updateDto.getLocation()).ifPresent(protest::setLocation);
            Optional.ofNullable(updateDto.getDeclaredParticipants())
                    .ifPresent(protest::setDeclaredParticipants);
            Optional.ofNullable(updateDto.getOrganizer()).ifPresent(protest::setOrganizer);
            Optional.ofNullable(updateDto.getStatus()).ifPresent(protest::setStatus);
        }
    }

    private Protest getProtestById(Long id) {
        return protestRepository.findById(id).orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    private void setLocationMappings(List<ProtestCreateMapping> protestCreateMappings) {
        int sequence = 0;
        for (ProtestCreateMapping mapping : protestCreateMappings) {
            Protest protest = mapping.getProtest();
            ProtestCreateRequest protestCreateRequest = mapping.getProtestCreateRequest();
            for (LocationResponse locationDto : protestCreateRequest.locations()) {
                // Retrieve or create location
                Location location = getOrCreateLocation(locationDto);

                // Create and add mapping
                createAndAddMapping(protest, location, sequence++);
            }
        }
    }

    private Location getOrCreateLocation(LocationResponse locationDto) {
        return locationRepository
                .findByName(locationDto.locationName())
                .orElseGet(
                        () ->
                                locationRepository.save(
                                        Location.builder()
                                                .name(locationDto.locationName())
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

    private List<LocationResponse> getLocations(Protest protest) {
        return protest.getLocationMappings().stream()
                .sorted(Comparator.comparing(ProtestLocationMapping::getSequence))
                .map(mapping -> LocationResponse.from(mapping.getLocation()))
                .toList();
    }

    private ProtestLocationDto getProtestLocationDto(Protest protest) {
        Long id = protest.getId();
        return locationRepository
                .findFirstLocationByProtestId(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    public Boolean participateVerify(Long protestId, ParticipateVerificationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Protest protest = getProtestById(protestId);

        ProtestLocationDto protestLocationDto = getProtestLocationDto(protest);

        Double distance =
                locationRepository.calculateDistance(
                        request.getLatitude(),
                        request.getLongitude(),
                        protestLocationDto.locationId());

        if (distance == null) throw ProtestNotFoundException.EXCEPTION;

        boolean isWithInRadius = distance <= protest.getRadius();

        if (!isWithInRadius) throw OutOfValidProtestRangeException.EXCEPTION;

        User userRef = userRepository.getReferenceById(userId);
        try {
            ParticipantsVerification verification =
                    ParticipantsVerification.builder().user(userRef).protest(protest).build();
            participantVerificationRepository.save(verification);
        } catch (DataIntegrityViolationException e) {
            throw DuplicateVerificationException.EXCEPTION;
        }

        return true;
    }
}
