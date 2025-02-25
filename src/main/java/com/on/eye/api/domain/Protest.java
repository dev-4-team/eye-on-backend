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

    @OneToMany(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<ProtestLocationMapping> locationMappings = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Organizer organizer;

    @OneToOne(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProtestVerification protestVerification;

    @Column(nullable = false)
    @Min(1)
    @Max(5000000)
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
            Organizer organizer,
            Integer declaredParticipants,
            Integer radius) {
        this.title = title;
        this.description = description;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.organizer = organizer;
        this.declaredParticipants = declaredParticipants;
        this.radius = radius;
    }
}
