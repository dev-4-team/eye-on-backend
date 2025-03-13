package com.on.eye.api.location.dto;

import java.math.BigDecimal;

public record ProtestLocationDto(
        Long protestId,
        Integer radius,
        Long locationId,
        BigDecimal latitude,
        BigDecimal longitude) {}
