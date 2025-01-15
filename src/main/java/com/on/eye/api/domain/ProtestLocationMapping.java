package com.on.eye.api.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "protest_location_mappings",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"protest_id", "sequence"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtestLocationMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protest_id", nullable = false)
    private Protest protest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private Integer sequence;

    @Builder
    public ProtestLocationMapping(Protest protest, Location location, Integer sequence) {
        this.protest = protest;
        this.location = location;
        this.sequence = sequence;
    }
}