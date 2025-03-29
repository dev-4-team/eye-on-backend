package com.on.eye.api.participant_verification.service;

import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.global.common.util.LocalDateTimeUtils;
import com.on.eye.api.global.config.security.AnonymousIdGenerator;
import com.on.eye.api.global.config.security.SecurityUtils;
import com.on.eye.api.participant_verification.dto.VerificationHistory;
import com.on.eye.api.participant_verification.entity.ParticipantsVerification;
import com.on.eye.api.participant_verification.repository.ParticipantVerificationRepository;
import com.on.eye.api.protest.dto.Coordinate;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.error.exception.DuplicateVerificationException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantVerificationService {
    private final ParticipantVerificationRepository participantVerificationRepository;
    private final AnonymousIdGenerator anonymousIdGenerator;

    @Transactional
    public void participateVerify(Protest protest, Coordinate userCoordinate) {
        Long userId = SecurityUtils.getCurrentUserId();
        String anonymousUserId = anonymousIdGenerator.generateAnonymousUserId(userId);

        // 가장 최근 인증 기록과, 현재 위치에 대한 비정상 이동 패턴 검출
        Optional<VerificationHistory> mostRecentVerification =
                participantVerificationRepository.findMostRecentVerification(
                        LocalDateTimeUtils.todayStartTime(), anonymousUserId);
        mostRecentVerification.ifPresent(
                verificationHistory ->
                        verificationHistory.detectAbnormalMovementPattern(userCoordinate));

        ParticipantsVerification verification =
                new ParticipantsVerification(protest, anonymousUserId);
        try {
            participantVerificationRepository.save(verification);
        } catch (DataIntegrityViolationException e) {
            // 중복 인증 검증
            throw DuplicateVerificationException.EXCEPTION;
        }
    }
}
