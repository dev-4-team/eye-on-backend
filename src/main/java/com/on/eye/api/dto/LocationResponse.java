package com.on.eye.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.on.eye.api.domain.Location;

public record LocationResponse(
        @NotEmpty(message = "locationName cannot be empty") String locationName,
        @NotNull(message = "longitude cannot be null")
                @Digits(
                        integer = 3,
                        fraction = 14,
                        message =
                                "longitude must have up to 3 integer digits and 14 fractional digits")
                BigDecimal latitude,
        @NotNull(message = "latitude cannot be null")
                @Digits(
                        integer = 3,
                        fraction = 14,
                        message =
                                "latitude must have up to 3 integer digits and 14 fractional digits")
                BigDecimal longitude) {

    public static LocationResponse from(Location location) {
        return new LocationResponse(
                location.getName(), location.getLatitude(), location.getLongitude());
    }
}
