package com.on.eye.api.repository;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ProtestRepository extends JpaRepository<Protest, Long> {
    // 상태별 시위 목록 조회
    List<Protest> findByStatus(ProtestStatus status);

    // 특정 기간 내의 시위 목록 조회
    List<Protest> findByStartDateTimeBetween(LocalDateTime startDateTime, LocalDateTime endDateTime);

    // 특정 장소에서 진행되는 시위 목록 조회
    List<Protest> findByLocation(String location);
}
