package com.on.eye.api.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.on.eye.api.domain.ParticipantsVerification;
import com.on.eye.api.dto.VerificationHistory;

public interface ParticipantVerificationRepository
        extends JpaRepository<ParticipantsVerification, Long> {
    List<ParticipantsVerification> getParticipantsVerificationByProtest_Id(Long protestId);

    @Query(
            """
                    SELECT new com.on.eye.api.dto.VerificationHistory(:anonUserId, p.id, l.latitude, l.longitude, pv.verifiedAt)  FROM ParticipantsVerification pv
                                JOIN pv.protest p
                                JOIN p.locationMappings plm
                                JOIN plm.location l
                            WHERE pv.anonymousUserId = :anonUserId and pv.protest.startDateTime >= :today
                            ORDER BY pv.verifiedAt DESC
                            LIMIT 1
                    """)
    Optional<VerificationHistory> getVerifiedParticipantsByDateTime(
            LocalDateTime today, String anonUserId);
}
