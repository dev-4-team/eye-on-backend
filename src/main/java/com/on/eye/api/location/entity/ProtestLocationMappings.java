package com.on.eye.api.location.entity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;

import org.springframework.lang.NonNull;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.error.exception.LocationNotFoundException;
import com.on.eye.api.protest.dto.Coordinate;
import com.on.eye.api.protest.entity.Protest;

import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
public class ProtestLocationMappings implements Iterable<ProtestLocationMapping> {
    @OneToMany(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private final List<ProtestLocationMapping> mappings = new ArrayList<>();

    public ProtestLocationMappings(Protest protest, Locations locations) {
        this.add(protest, locations);
    }

    public Coordinate getCenterCoordinate() {
        if (mappings.isEmpty() || mappings.get(0).getLocation() == null) {
            throw LocationNotFoundException.EXCEPTION;
        }
        return mappings.get(0).getLocation().toCoordinate();
    }

    private void add(Protest protest, Locations locations) {
        int nextSequence = mappings.size();
        for (Location location : locations) {
            mappings.add(new ProtestLocationMapping(protest, location, nextSequence++));
        }
    }

    public List<LocationDto> toLocationDtos() {
        return mappings.stream()
                .sorted(Comparator.comparingInt(ProtestLocationMapping::getSequence))
                .map(mapping -> mapping.getLocation().toDto())
                .toList();
    }

    @Override
    @NonNull
    public Iterator<ProtestLocationMapping> iterator() {
        return mappings.iterator();
    }
}
