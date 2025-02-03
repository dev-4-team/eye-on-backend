package com.on.eye.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protests")
@Getter
@Setter
@NoArgsConstructor
public class Protest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Column(nullable = false)
    private String location; // 집회 장소(행진로)

    @OneToMany(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<ProtestLocationMapping> locationMappings = new ArrayList<>();

    private String organizer;

    @Column(nullable = false)
    @Min(50)
    @Max(500)
    private Integer declaredParticipants;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ProtestStatus status = ProtestStatus.SCHEDULED;

    @Column(nullable = false)
    private Integer radius = 500;

    @Builder
    public Protest(
            String title,
            String description,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String location,
            String organizer,
            Integer declaredParticipants,
            Integer radius) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.location = location;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.radius = radius;
    }
}
