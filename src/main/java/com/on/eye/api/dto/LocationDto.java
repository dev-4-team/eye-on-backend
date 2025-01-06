package com.on.eye.api.dto;

import com.on.eye.api.domain.Location;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class LocationDto {
    @NotEmpty(message = "locationName cannot be empty")
    private final String locationName;

    @NotNull(message = "longitude cannot be null")
    @Digits(integer = 3, fraction = 14, message = "longitude must have up to 3 integer digits and 14 fractional digits")
    BigDecimal latitude;

    @NotNull(message = "latitude cannot be null")
    @Digits(integer = 3, fraction = 14, message = "latitude must have up to 3 integer digits and 14 fractional digits")
    BigDecimal longitude;

    @Builder
    public LocationDto(String locationName, BigDecimal latitude, BigDecimal longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static LocationDto from(Location location) {
        return LocationDto.builder().
                locationName(location.getName()).
                latitude(location.getLatitude()).
                longitude(location.getLongitude()).
                build();
    }
}
