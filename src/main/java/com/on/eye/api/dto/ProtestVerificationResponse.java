package com.on.eye.api.dto;

import com.on.eye.api.domain.ProtestVerification;

public record ProtestVerificationResponse(Long protestId, int verifiedNum) {

    public static ProtestVerificationResponse from(ProtestVerification protestVerification) {
        return new ProtestVerificationResponse(
                protestVerification.getProtest().getId(), protestVerification.getVerifiedNum());
    }
}
