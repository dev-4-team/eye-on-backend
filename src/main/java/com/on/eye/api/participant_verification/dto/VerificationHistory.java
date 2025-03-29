package com.on.eye.api.participant_verification.dto;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import com.on.eye.api.protest.dto.Coordinate;
import com.on.eye.api.protest.error.exception.AbnormalMovementPatternException;
import com.on.eye.api.protest.util.GeoUtils;

public record VerificationHistory(
        Long protestId, BigDecimal latitude, BigDecimal longitude, LocalDateTime verifiedAt) {

    private static final double MAX_SPEED_MPS = 16; // 약 시속 60km

    public void detectAbnormalMovementPattern(Coordinate userCoordinate) {
        double distanceDiff = GeoUtils.haversineDistance(userCoordinate, toCoordinate());
        long timeDiff = Duration.between(verifiedAt, LocalDateTime.now()).getSeconds();
        if (timeDiff < 1) timeDiff = 1;

        double speedInMeterPerSec = distanceDiff / timeDiff;

        if (speedInMeterPerSec > MAX_SPEED_MPS) {
            throw AbnormalMovementPatternException.EXCEPTION;
        }
    }

    public Coordinate toCoordinate() {
        return new Coordinate(latitude, longitude);
    }
}
