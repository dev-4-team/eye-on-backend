package com.on.eye.api.protest_verification.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.global.common.util.LocalDateTimeUtils;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest_verification.dto.ProtestVerificationResponse;
import com.on.eye.api.protest_verification.entity.ProtestVerification;
import com.on.eye.api.protest_verification.repository.ProtestVerificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProtestVerificationService {
    private final ProtestVerificationRepository protestVerificationRepository;

    @Transactional
    public void updateProtestVerification(Protest protest) {
        protestVerificationRepository.increaseVerifiedNum(protest.getId());
    }

    @Transactional(readOnly = true)
    public List<ProtestVerificationResponse> getTodayProtestVerifications(
            Long protestId, LocalDate date) {
        LocalDateTime todayStartTime = LocalDateTimeUtils.getStartOfDay(date.atStartOfDay());
        LocalDateTime todayEndTime = LocalDateTimeUtils.getEndOfDay(date.atStartOfDay());
        if (isRequestAll(protestId))
            return protestVerificationRepository.findAllByProtestDateTimeBetween(
                    todayStartTime, todayEndTime);
        ProtestVerification verification = protestVerificationRepository.findByProtestId(protestId);
        return List.of(ProtestVerificationResponse.from(verification));
    }

    private boolean isRequestAll(Long protestId) {
        return protestId == null;
    }
}
