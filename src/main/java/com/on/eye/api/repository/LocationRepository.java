package com.on.eye.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.domain.Location;
import com.on.eye.api.dto.ProtestLocationDto;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);

    /**
             * Retrieves the most similar location based on the provided search string.
             *
             * <p>This method executes a native SQL query that uses the {@code bigm_similarity} function to compare
             * location names to the given {@code searchName}. It orders the results in descending order of similarity
             * and returns only the top result. An {@code Optional<Location>} is returned, which is empty if no location
             * exceeds the specified similarity {@code threshold}.
             *
             * @param searchName the string to compare against location names
             * @param threshold the minimum similarity value required for a match
             * @return an {@code Optional} containing the most similar location if one exists, otherwise an empty {@code Optional}
             */
            @Query(
            value =
                    "SELECT * FROM locations "
                            + "WHERE bigm_similarity(name, :searchName) > :threshold "
                            + "ORDER BY bigm_similarity(name, :searchName) DESC "
                            + "LIMIT 1",
            nativeQuery = true)
    Optional<Location> findMostSimilarLocation(
            @Param("searchName") String searchName, @Param("threshold") double threshold);

    /**
     * Retrieves the first location mapping for the specified protest.
     * <p>
     * Executes a JPQL query that joins a protest with its location mappings and locations,
     * ordering the mappings by sequence and selecting the first result. Constructs a
     * {@code ProtestLocationDto} containing the protest’s ID, radius, and the associated
     * location’s ID, latitude, and longitude.
     * </p>
     *
     * @param protestId the ID of the protest for which to fetch the first location mapping
     * @return an {@code Optional} containing the corresponding {@code ProtestLocationDto} if found,
     *         or an empty {@code Optional} if no matching protest is found
     */
    @Query(
            """
                            select new com.on.eye.api.dto.ProtestLocationDto(
                                p.id,
                                p.radius,
                                l.id,
                                l.latitude,
                                l.longitude
                            )
                            from Protest p
                            join p.locationMappings lm
                            join lm.location l
                            where p.id = :protestId
                            order by lm.sequence
                            limit 1
                    """)
    Optional<ProtestLocationDto> findFirstLocationByProtestId(@Param("protestId") Long protestId);
}
