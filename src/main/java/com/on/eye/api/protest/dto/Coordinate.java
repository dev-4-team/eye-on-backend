package com.on.eye.api.protest.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record Coordinate(@NotNull BigDecimal latitude, @NotNull BigDecimal longitude) {}
