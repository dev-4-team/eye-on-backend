package com.on.eye.api.organizer.service;

import org.springframework.stereotype.Service;

import com.on.eye.api.organizer.dto.OrganizerRequest;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.repository.OrganizerRepository;
import com.on.eye.api.protest.dto.ProtestCreateRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerService {
    private final OrganizerRepository organizerRepository;
    private static final double SIMILARITY_THRESHOLD = 0.35;

    public Organizer getOrCreateOrganizer(ProtestCreateRequest request) {
        OrganizerRequest organizerRequest =
                new OrganizerRequest(request.organizer(), request.title());
        return getOrCreateOrganizer(organizerRequest);
    }

    private Organizer getOrCreateOrganizer(OrganizerRequest organizerRequest) {
        return organizerRepository
                .findBySimilarOrganizer(organizerRequest.name(), SIMILARITY_THRESHOLD)
                .orElseGet(() -> organizerRepository.save(Organizer.from(organizerRequest)));
    }
}
