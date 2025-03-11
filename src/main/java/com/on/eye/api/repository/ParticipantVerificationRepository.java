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
    /**
 * Retrieves ParticipantsVerification records associated with the specified protest.
 *
 * @param protestId the unique identifier of the protest for which verification records are retrieved
 * @return a list of ParticipantsVerification records linked to the specified protest
 */
List<ParticipantsVerification> getParticipantsVerificationByProtest_Id(Long protestId);

    /**
             * Retrieves the most recent verification history for the specified anonymous user.
             *
             * <p>This method executes a JPQL query to create a {@code VerificationHistory} DTO by joining the participants
             * verification, protest, location mapping, and location entities. It filters records by the provided anonymous
             * user ID and includes only those where the protest's start date/time is on or after the specified {@code today}
             * parameter. The result is ordered by the verification timestamp in descending order and limited to the most recent entry.
             * If no matching record is found, an empty {@code Optional} is returned.</p>
             *
             * @param today the lower bound for the protest start date/time; only protests starting on or after this date/time are considered
             * @param anonUserId the anonymous identifier of the participant
             * @return an {@code Optional} containing the latest {@code VerificationHistory} if available, otherwise empty
             */
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
