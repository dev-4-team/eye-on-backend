package com.on.eye.api.service;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestDetailDto;
import com.on.eye.api.dto.ProtestListItemDto;
import com.on.eye.api.dto.ProtestUpdateDto;
import com.on.eye.api.exception.ResourceNotFoundException;
import com.on.eye.api.mapper.ProtestMapper;
import com.on.eye.api.repository.ProtestRepository;
import com.on.eye.api.util.Constants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProtestService {
    private final ProtestRepository protestRepository;
    private static final String RESOURCE_NAME = "Protest";

    public Protest createProtest(ProtestCreateDto protestCreateDto) {
        // 생성 시간 기준으로 상태 자동 설정
        Protest protest = ProtestMapper.toEntity(protestCreateDto);
        return protestRepository.save(protest);
    }

    public ProtestDetailDto getProtestDetail(Long id) {
        Protest protest = getProtestById(id);
        return ProtestMapper.toDto(protest);
    }

    public List<ProtestListItemDto> getProtestsBy(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return protestRepository.findByStartDateTimeBetween(startOfDay, endOfDay)
                .stream()
                .map(ProtestListItemDto::from)
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
            Optional.ofNullable(updateDto.getDeclaredParticipants()).ifPresent(protest::setDeclaredParticipants);
            Optional.ofNullable(updateDto.getOrganizer()).ifPresent(protest::setOrganizer);
            Optional.ofNullable(updateDto.getStatus()).ifPresent(protest::setStatus);
        }
    }

    private Protest getProtestById(Long id) {
        return protestRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(RESOURCE_NAME, Constants.ID, id));
    }
}
