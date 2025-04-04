package com.on.eye.api.cheer.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.on.eye.api.cheer.entity.ProtestCheerCount;

public interface ProtestCheerCountRepository extends JpaRepository<ProtestCheerCount, Long> {
    Optional<ProtestCheerCount> findByProtestId(Long protestId);

    @Query("SELECT pc from ProtestCheerCount pc WHERE pc.protestId IN :protestIds")
    List<ProtestCheerCount> findAllByProtestIds(@Param("protestIds") List<Long> protestIds);

    @Query(
            "SELECT pc FROM ProtestCheerCount pc WHERE pc.protestId IN (SELECT p.id FROM Protest p WHERE p.startDateTime >= :startDateTime and p.endDateTime < :endDateTime)")
    List<ProtestCheerCount> findAllByProtestBetweenStartDateTimeAndEndDateTime(
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Modifying
    @Query(
            "UPDATE ProtestCheerCount pc SET pc.cheerCount = pc.cheerCount + 1 WHERE pc.protestId = :protestId")
    Integer incrementCheerCount(@Param("protestId") Long protestId);
}
