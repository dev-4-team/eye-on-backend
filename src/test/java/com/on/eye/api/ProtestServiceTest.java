package com.on.eye.api;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.repository.ProtestRepository;
import com.on.eye.api.service.ProtestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProtestServiceTest {
    @Autowired
    private ProtestService protestService;

    @Autowired
    private ProtestRepository protestRepository;

    @BeforeEach
    void setUp() {
        protestRepository.deleteAll();
    }

    @Test
    void createProtest_WhenStartDateIsInFuture_StatusShouldBeScheduled() {
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        // Given
        ProtestCreateDto protestCreateDto = stubProtestCreateDto(startDateTime);

        // When
        Protest savedProtest = protestService.createProtest(protestCreateDto);

        // Then
        assertThat(savedProtest.getId()).isNotNull();
        assertThat(savedProtest.getStatus()).isEqualTo(ProtestStatus.SCHEDULED);

        // DB에도 제대로 저장되었는지 확인
        Protest foundProtest = protestRepository.findById(savedProtest.getId())
                .orElseThrow();
        assertThat(foundProtest.getStatus()).isEqualTo(ProtestStatus.SCHEDULED);
    }

    ProtestCreateDto stubProtestCreateDto (LocalDateTime startDateTime) {
        LocalDateTime endDateTime = startDateTime.plusHours(3);
        return ProtestCreateDto.builder()
                .title("탄핵 시위")
                .description("윤석열 탄핵 찬성")
                .location("여의도")
                .organizer("국민촛불행동")
                .declaredParticipants(100)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .build();
    }
}
