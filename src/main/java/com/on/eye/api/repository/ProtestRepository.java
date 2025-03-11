package com.on.eye.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.domain.Protest;

public interface ProtestRepository extends JpaRepository<Protest, Long> {
    /**
 * Retrieves a list of protests with a start date and time after the specified value.
 *
 * @param startDateTime the reference date and time; only protests starting after this are returned
 * @return a list of Protest entities that commence after the given start date and time
 */
List<Protest> findByStartDateTimeAfter(LocalDateTime startDateTime);

    /**
     * Retrieves a protest along with its organizer by protest ID.
     *
     * <p>This method performs a JPQL query that left joins the organizer, eagerly fetching the organizer 
     * associated with the protest. It returns an {@code Optional} containing the protest if found, or 
     * an empty {@code Optional} if there is no protest with the provided ID.
     *
     * @param protestId the unique identifier of the protest to be retrieved
     * @return an {@code Optional} containing the protest with its organizer, or empty if not found
     */
    @Query("SELECT p FROM Protest p LEFT JOIN FETCH p.organizer WHERE p.id = :protestId")
    Optional<Protest> findByProtestIdWithOrganizer(@Param("protestId") Long protestId);

    @Query(
            "SELECT DISTINCT p from Protest p LEFT JOIN FETCH p.organizer WHERE p.startDateTime >= :startDateTime and p.endDateTime < :endDateTime")
    List<Protest> findByStartDateTimeAfterWithOrganizer(
            @Param("startDateTime") LocalDateTime startDateTime, LocalDateTime endDateTime);
}
