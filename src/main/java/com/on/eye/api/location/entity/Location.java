package com.on.eye.api.location.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.protest.dto.Coordinate;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "locations",
        indexes = {@Index(name = "location_name", columnList = "name", unique = true)})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Location {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // protest의 location과 겹침

    @Column(precision = 17, scale = 14, nullable = false)
    private BigDecimal latitude;

    @Column(precision = 17, scale = 14, nullable = false)
    private BigDecimal longitude;

    @Builder
    public Location(String name, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Coordinate toCoordinate() {
        return new Coordinate(this.latitude, this.longitude);
    }

    public LocationDto toDto() {
        return new LocationDto(this.name, this.latitude, this.longitude);
    }

    public static Location from(LocationDto dto) {
        return Location.builder()
                .name(dto.name())
                .latitude(dto.latitude())
                .longitude(dto.longitude())
                .build();
    }
}
