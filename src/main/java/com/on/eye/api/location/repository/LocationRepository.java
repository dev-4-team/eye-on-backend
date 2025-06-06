package com.on.eye.api.location.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.location.dto.ProtestLocationDto;
import com.on.eye.api.location.entity.Location;

public interface LocationRepository extends JpaRepository<Location, Long> {
    Optional<Location> findByName(String name);

    @Query(
            value =
                    "SELECT * FROM locations "
                            + "WHERE bigm_similarity(name, :searchName) > :threshold "
                            + "ORDER BY bigm_similarity(name, :searchName) DESC "
                            + "LIMIT 1",
            nativeQuery = true)
    Optional<Location> findMostSimilarLocation(
            @Param("searchName") String searchName, @Param("threshold") double threshold);

    @Query(
            """
                            select new com.on.eye.api.location.dto.ProtestLocationDto(
                                p.id,
                                p.radius,
                                l.id,
                                l.latitude,
                                l.longitude
                            )
                            from Protest p
                            join p.locationMappings.mappings lm
                            join lm.location l
                            where p.id = :protestId
                            order by lm.sequence
                            limit 1
                    """)
    Optional<ProtestLocationDto> findFirstLocationByProtestId(@Param("protestId") Long protestId);
}
