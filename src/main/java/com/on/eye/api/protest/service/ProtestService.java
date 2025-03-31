package com.on.eye.api.protest.service;

import com.on.eye.api.protest.dto.ProtestResponse;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.error.exception.ProtestNotFoundException;
import com.on.eye.api.protest.repository.ProtestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProtestService {
    private final ProtestRepository protestRepository;

    @Transactional(readOnly = true)
    public ProtestResponse getProtestDetail(Long id) {
        Protest protest = getProtestByIdWithOrganizer(id);

        return protest.toResponse();
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
                        .map(Protest::toResponse)
                        .toList();

        log.info("날짜 별 시위 조회 완료 - 날짜: {}, 조회된 시위: {}건", date, response.size());
        return response;
    }

    private Protest getProtestByIdWithOrganizer(Long id) {
        return protestRepository
                .findByProtestIdWithOrganizer(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    public Protest getProtestByIdWithLocations(Long id) {
        return protestRepository
                .findByProtestIdWithLocations(id)
                .orElseThrow(() -> ProtestNotFoundException.EXCEPTION);
    }

    public List<Long> saveAllProtests(List<Protest> protests) {
        return protestRepository.saveAll(protests).stream().map(Protest::getId).toList();
    }
}
