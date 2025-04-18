package com.on.eye.api.protest.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.protest.entity.Protest;

public interface ProtestRepository extends JpaRepository<Protest, Long> {
    List<Protest> findByStartDateTimeAfter(LocalDateTime startDateTime);

    @Query("SELECT p FROM Protest p LEFT JOIN FETCH p.organizer WHERE p.id = :protestId")
    Optional<Protest> findByProtestIdWithOrganizer(@Param("protestId") Long protestId);

    @Query(
            "SELECT DISTINCT p from Protest p LEFT JOIN FETCH p.organizer WHERE p.startDateTime >= :startDateTime and p.endDateTime < :endDateTime")
    List<Protest> findByStartDateTimeAfterWithOrganizer(
            @Param("startDateTime") LocalDateTime startDateTime, LocalDateTime endDateTime);

    @Query(
            """
                    SELECT DISTINCT p from Protest p
                    LEFT JOIN FETCH p.locationMappings.mappings lm
                    LEFT JOIN FETCH lm.location
                    WHERE p.id = :protestId
                    """)
    Optional<Protest> findByProtestIdWithLocations(Long protestId);
}
