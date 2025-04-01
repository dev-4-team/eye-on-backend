package com.on.eye.api.organizer.entity;

import jakarta.persistence.*;

import com.on.eye.api.global.common.model.entity.BaseTimeEntity;
import com.on.eye.api.organizer.dto.OrganizerRequest;
import com.on.eye.api.organizer.dto.OrganizerResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "organizers",
        indexes = {@Index(name = "organizer_name", columnList = "name", unique = true)})
@NoArgsConstructor
@Getter
public class Organizer extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String title;

    @Builder
    public Organizer(String name, String description, String title) {
        this.name = name;
        this.description = description;
        this.title = title;
    }

    public static Organizer from(OrganizerRequest dto) {
        return new Organizer(dto.name(), "", dto.title());
    }

    public OrganizerResponse toResponse() {
        return new OrganizerResponse(this.name, this.title, this.description);
    }
}
