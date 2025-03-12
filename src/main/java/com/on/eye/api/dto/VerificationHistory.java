package com.on.eye.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record VerificationHistory(
        Long protestId, BigDecimal latitude, BigDecimal longitude, LocalDateTime verifiedAt) {}
