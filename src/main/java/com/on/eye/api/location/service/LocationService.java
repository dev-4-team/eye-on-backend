package com.on.eye.api.location.service;

import org.springframework.stereotype.Service;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.dto.ProtestLocationDto;
import com.on.eye.api.location.entity.Location;
import com.on.eye.api.location.error.exception.LocationNotFoundException;
import com.on.eye.api.location.repository.LocationRepository;
import com.on.eye.api.protest.entity.Protest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    private final LocationRepository locationRepository;

    public Location getOrCreateLocation(LocationDto locationDto) {
        double similarity = 0.35;
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

    public ProtestLocationDto getProtestCenterLocation(Protest protest) {
        Long protestId = protest.getId();
        return locationRepository
                .findFirstLocationByProtestId(protestId)
                .orElseThrow(() -> LocationNotFoundException.EXCEPTION);
    }
}
