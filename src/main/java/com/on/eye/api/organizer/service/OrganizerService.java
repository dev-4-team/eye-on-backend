package com.on.eye.api.organizer.service;

import com.on.eye.api.organizer.dto.OrganizerDto;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.repository.OrganizerRepository;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerService {
    private final OrganizerRepository organizerRepository;
    private static final double SIMILARITY_THRESHOLD = 0.35;

    public Organizer getOrCreateOrganizer(ProtestCreateRequest request) {
        OrganizerDto organizerDto = new OrganizerDto(request.organizer(), request.title());
        return getOrCreateOrganizer(organizerDto);
    }

    private Organizer getOrCreateOrganizer(OrganizerDto organizerDto) {
        return organizerRepository
                .findBySimilarOrganizer(organizerDto.name(), SIMILARITY_THRESHOLD)
                .orElseGet(() -> organizerRepository.save(Organizer.from(organizerDto)));
    }
}
