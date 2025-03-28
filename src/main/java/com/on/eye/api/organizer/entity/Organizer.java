package com.on.eye.api.organizer.entity;

import jakarta.persistence.*;

import com.on.eye.api.organizer.dto.OrganizerDto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "organizers",
        indexes = {@Index(name = "organizer_name", columnList = "name", unique = true)})
@NoArgsConstructor
@Getter
public class Organizer {
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

    public static Organizer from(OrganizerDto dto) {
        return new Organizer(dto.name(), null, dto.title());
    }
}
