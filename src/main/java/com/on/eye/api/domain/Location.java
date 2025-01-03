package com.on.eye.api.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(
        name = "locations",
        indexes = {
                @Index(name = "location_name", columnList = "name", unique = true)
        }
)
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
}
