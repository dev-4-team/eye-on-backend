package com.on.eye.api.location.service;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.dto.ProtestLocationDto;
import com.on.eye.api.location.entity.Location;
import com.on.eye.api.location.entity.Locations;
import com.on.eye.api.location.entity.ProtestLocationMappings;
import com.on.eye.api.location.error.exception.LocationNotFoundException;
import com.on.eye.api.location.repository.LocationRepository;
import com.on.eye.api.protest.entity.Protest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {
    private final LocationRepository locationRepository;

    public ProtestLocationMappings assignLocationMappings(
            Protest protest, List<LocationDto> locationDtos) {
        Locations locations = getLocations(locationDtos);
        return new ProtestLocationMappings(protest, locations);
    }

    private Locations getLocations(List<LocationDto> locationDtos) {
        Locations locations = new Locations();
        for (LocationDto locationDto : locationDtos) {
            Location location = getOrCreateLocation(locationDto);
            locations.add(location);
        }

        return locations;
    }

    private Location getOrCreateLocation(LocationDto locationDto) {
        return locationRepository
                .findByName(locationDto.name())
                .orElseGet(() -> locationRepository.save(Location.from(locationDto)));
    }

    public ProtestLocationDto getProtestCenterLocation(Protest protest) {
        Long protestId = protest.getId();
        return locationRepository
                .findFirstLocationByProtestId(protestId)
                .orElseThrow(() -> LocationNotFoundException.EXCEPTION);
    }
}
