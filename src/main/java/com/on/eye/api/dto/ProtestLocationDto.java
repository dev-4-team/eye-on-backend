package com.on.eye.api.dto;

import java.math.BigDecimal;

public record ProtestLocationDto(
        Long protestId,
        Integer radius,
        Long locationId,
        BigDecimal latitude,
        BigDecimal longitude) {}
