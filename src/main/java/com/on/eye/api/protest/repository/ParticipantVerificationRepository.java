package com.on.eye.api.protest.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.on.eye.api.protest.dto.VerificationHistory;
import com.on.eye.api.protest.entity.ParticipantsVerification;

public interface ParticipantVerificationRepository
        extends JpaRepository<ParticipantsVerification, Long> {
    @Query(
            """
                    SELECT new com.on.eye.api.protest.dto.VerificationHistory(p.id, l.latitude, l.longitude, pv.verifiedAt)  FROM ParticipantsVerification pv
                                JOIN pv.protest p
                                JOIN p.locationMappings plm
                                JOIN plm.location l
                            WHERE pv.anonymousUserId = :anonUserId and p.startDateTime >= :today
                            ORDER BY pv.verifiedAt DESC
                            FETCH FIRST 1 ROW ONLY
                    """)
    Optional<VerificationHistory> findMostRecentVerificationByUserSince(
            LocalDateTime today, String anonUserId);
}
