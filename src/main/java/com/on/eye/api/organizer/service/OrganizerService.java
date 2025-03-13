package com.on.eye.api.organizer.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.repository.OrganizerRepository;
import com.on.eye.api.protest.dto.ProtestCreateMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizerService {
    private final OrganizerRepository organizerRepository;

    public void checkOrganizer(List<ProtestCreateMapping> protestCreateMappings) {
        double threshold = 0.35;
        protestCreateMappings.forEach(
                createMapping ->
                        organizerRepository
                                .findBySimilarOrganizer(
                                        createMapping.getProtestCreateRequest().organizer(),
                                        threshold)
                                .ifPresentOrElse(
                                        organizer ->
                                                createMapping.getProtest().setOrganizer(organizer),
                                        () -> {
                                            Organizer organizer =
                                                    organizerRepository.save(
                                                            Organizer.builder()
                                                                    .name(
                                                                            createMapping
                                                                                    .getProtestCreateRequest()
                                                                                    .organizer())
                                                                    .title(
                                                                            createMapping
                                                                                    .getProtestCreateRequest()
                                                                                    .title())
                                                                    .build());
                                            createMapping.getProtest().setOrganizer(organizer);
                                        }));
    }
}
