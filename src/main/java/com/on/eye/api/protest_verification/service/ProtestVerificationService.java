package com.on.eye.api.protest_verification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.on.eye.api.protest.entity.Protest;
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
}
