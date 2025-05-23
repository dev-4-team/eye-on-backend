package com.on.eye.api.participant_verification.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.on.eye.api.participant_verification.dto.VerificationHistory;
import com.on.eye.api.participant_verification.entity.ParticipantsVerification;

public interface ParticipantVerificationRepository
        extends JpaRepository<ParticipantsVerification, Long> {
    @Query(
            """
                    SELECT new com.on.eye.api.participant_verification.dto.VerificationHistory(p.id, l.latitude, l.longitude, pv.verifiedAt)  FROM ParticipantsVerification pv
                                JOIN pv.protest p
                                JOIN p.locationMappings.mappings plm
                                JOIN plm.location l
                            WHERE pv.anonymousUserId = :anonUserId and p.startDateTime >= :today
                            ORDER BY pv.verifiedAt DESC
                            FETCH FIRST 1 ROW ONLY
                    """)
    Optional<VerificationHistory> findMostRecentVerification(
            LocalDateTime today, String anonUserId);
}
