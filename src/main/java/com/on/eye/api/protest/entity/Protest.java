package com.on.eye.api.protest.entity;

import com.on.eye.api.location.entity.ProtestLocationMappings;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "protests")
@Getter
@NoArgsConstructor
public class Protest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    @Column(nullable = false, updatable = false)
    private final LocalDateTime createTime = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime startDateTime;

    @Column(nullable = false)
    private LocalDateTime endDateTime;

    @Embedded
    private ProtestLocationMappings locationMappings = new ProtestLocationMappings();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private Organizer organizer;

    @OneToOne(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProtestVerification protestVerification;

    @OneToMany(mappedBy = "protest", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ParticipantsVerification> participantsVerifications = new ArrayList<>();

    @Column(nullable = false)
    @Min(1)
    @Max(5000000)
    private Integer declaredParticipants;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private final ProtestStatus status = ProtestStatus.SCHEDULED;

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

    public void addLocationMappings(ProtestLocationMappings locationMappings) {
        this.locationMappings = locationMappings;
    }

    public void addOrganizer(Organizer organizer) {
        this.organizer = organizer;
    }

    public void addVerification() {
        this.protestVerification = new ProtestVerification(this);
    }

    public static Protest from(ProtestCreateRequest protestCreateRequest) {
        return Protest.builder()
                .title(protestCreateRequest.title())
                .startDateTime(protestCreateRequest.startDateTime())
                .endDateTime(protestCreateRequest.endDateTime())
                .declaredParticipants(protestCreateRequest.declaredParticipants())
                .radius(calRadius(protestCreateRequest.declaredParticipants()))
                .build();
    }

    private static Integer calRadius(int declaredParticipants) {
        // 최소/최대 제한 설정
        if (declaredParticipants < 10) return 10;
        if (declaredParticipants > 500000) return 500;

        // 로그 스케일 변환을 위한 기본값 설정
        final double MIN_PARTICIPANTS = Math.log(10);
        final double MAX_PARTICIPANTS = Math.log(500000);
        final double MIN_RADIUS = 10;
        final double MAX_RADIUS = 500;

        // 로그 스케일 변환 후 선형 매핑
        double logParticipants = Math.log(declaredParticipants);
        double radius =
                MIN_RADIUS
                        + (logParticipants - MIN_PARTICIPANTS)
                        * (MAX_RADIUS - MIN_RADIUS)
                        / (MAX_PARTICIPANTS - MIN_PARTICIPANTS);

        // 정수로 반올림
        return (int) Math.round(radius);
    }
}
