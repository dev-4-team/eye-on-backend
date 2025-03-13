package com.on.eye.api.organizer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.organizer.entity.Organizer;

public interface OrganizerRepository extends JpaRepository<Organizer, Long> {
    @Query(
            value =
                    "SELECT * FROM organizers "
                            + "WHERE bigm_similarity(name, :organizerName) > :threshold "
                            + "ORDER BY bigm_similarity(name, :organizerName) DESC "
                            + "LIMIT 1",
            nativeQuery = true)
    Optional<Organizer> findBySimilarOrganizer(
            @Param("organizerName") String organizerName, @Param("threshold") double threshold);

    Optional<Organizer> findOrganizerByName(String name);
}
