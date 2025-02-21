package com.on.eye.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;

public interface ProtestRepository extends JpaRepository<Protest, Long> {
    // 상태별 시위 목록 조회
    List<Protest> findByStatus(ProtestStatus status);

    List<Protest> findByStartDateTimeBetween(
            LocalDateTime startDateTimeAfter, LocalDateTime startDateTimeBefore);

    List<Protest> findByStartDateTimeAfter(LocalDateTime startDateTime);

    @Query("SELECT p FROM Protest p LEFT JOIN FETCH p.organizer WHERE p.id = :protestId")
    Optional<Protest> findByProtestIdWithOrganizer(@Param("protestId") Long protestId);

    @Query(
            "SELECT DISTINCT p from Protest p LEFT JOIN FETCH p.organizer WHERE p.startDateTime >= :startDateTime and p.endDateTime < :endDateTime")
    List<Protest> findByStartDateTimeAfterWithOrganizer(
            @Param("startDateTime") LocalDateTime startDateTime, LocalDateTime endDateTime);
}
