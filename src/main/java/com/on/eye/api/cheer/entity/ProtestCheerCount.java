package com.on.eye.api.cheer.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import com.on.eye.api.cheer.dto.CheerStat;
import com.on.eye.api.global.common.model.entity.BaseTimeEntity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProtestCheerCount extends BaseTimeEntity {
    @Id @Column(unique = true)
    private Long protestId;

    @Column(nullable = false)
    private Integer cheerCount;

    @Builder
    public ProtestCheerCount(Long protestId, Integer cheerCount) {
        this.protestId = protestId;
        this.cheerCount = cheerCount;
    }

    public static ProtestCheerCount from(Long protestId) {
        return new ProtestCheerCount(protestId, 0);
    }

    public static ProtestCheerCount from(CheerStat cheerStat) {
        return new ProtestCheerCount(cheerStat.protestId(), cheerStat.cheerCount());
    }
}
