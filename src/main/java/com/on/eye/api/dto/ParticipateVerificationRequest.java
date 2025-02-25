package com.on.eye.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record ParticipateVerificationRequest(
        @NotNull BigDecimal longitude, @NotNull BigDecimal latitude) {}
