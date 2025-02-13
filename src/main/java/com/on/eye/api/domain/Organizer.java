package com.on.eye.api.domain;

import jakarta.persistence.*;

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

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
}
