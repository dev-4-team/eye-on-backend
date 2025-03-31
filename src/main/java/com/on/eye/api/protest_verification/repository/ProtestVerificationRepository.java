package com.on.eye.api.protest_verification.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.on.eye.api.protest_verification.dto.ProtestVerificationResponse;
import com.on.eye.api.protest_verification.entity.ProtestVerification;

public interface ProtestVerificationRepository extends JpaRepository<ProtestVerification, Long> {
    @Modifying
    @Query(
            "UPDATE ProtestVerification v SET v.verifiedNum = v.verifiedNum + 1 WHERE v.protest.id = :protestId")
    void increaseVerifiedNum(Long protestId);

    ProtestVerification findByProtestId(Long protestId);

    @Query(
            """
                    SELECT new com.on.eye.api.protest_verification.dto.ProtestVerificationResponse(p.id, pv.verifiedNum) FROM ProtestVerification pv
                        LEFT JOIN pv.protest p
                            WHERE p.startDateTime >= :startDateTime and p.endDateTime < :endDateTime
                    """)
    List<ProtestVerificationResponse> findAllByProtestDateTimeBetween(
            LocalDateTime startDateTime, LocalDateTime endDateTime);
}
