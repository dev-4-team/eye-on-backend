package com.on.eye.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.on.eye.api.domain.ParticipantsVerification;

public interface ParticipantVerificationRepository
        extends JpaRepository<ParticipantsVerification, Long> {}
