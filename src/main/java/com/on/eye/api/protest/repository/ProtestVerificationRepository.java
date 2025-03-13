package com.on.eye.api.protest.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.on.eye.api.protest.entity.ProtestVerification;

public interface ProtestVerificationRepository extends JpaRepository<ProtestVerification, Long> {
    @Modifying
    @Query(
            "UPDATE ProtestVerification v SET v.verifiedNum = v.verifiedNum + 1 WHERE v.protest.id = :protestId")
    void increaseVerifiedNum(Long protestId);

    ProtestVerification findByProtestId(Long protestId);
}
