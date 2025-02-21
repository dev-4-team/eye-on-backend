package com.on.eye.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import com.on.eye.api.domain.Location;

public record LocationDto(
        @NotEmpty(message = "location name cannot be empty") String name,
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

    public static LocationDto from(Location location) {
        return new LocationDto(location.getName(), location.getLatitude(), location.getLongitude());
    }
}
