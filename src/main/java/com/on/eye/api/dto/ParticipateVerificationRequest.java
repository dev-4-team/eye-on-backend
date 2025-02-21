package com.on.eye.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;

@Getter
public class ParticipateVerificationRequest {
    @NotNull private BigDecimal longitude;
    @NotNull private BigDecimal latitude;
}
