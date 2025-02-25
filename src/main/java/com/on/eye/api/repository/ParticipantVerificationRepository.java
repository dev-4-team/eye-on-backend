package com.on.eye.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.on.eye.api.domain.ParticipantsVerification;

public interface ParticipantVerificationRepository
        extends JpaRepository<ParticipantsVerification, Long> {
    List<ParticipantsVerification> getParticipantsVerificationByProtest_Id(Long protestId);
}
