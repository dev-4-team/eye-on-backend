package com.on.eye.api;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestDetailDto;
import com.on.eye.api.dto.ProtestUpdateDto;
import com.on.eye.api.repository.ProtestRepository;
import com.on.eye.api.service.ProtestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ProtestServiceTest {
    @Autowired
    private ProtestService protestService;

    @Autowired
    private ProtestRepository protestRepository;

    private ProtestCreateDto testProtestDto;

    @BeforeEach
    void setUp() {
        protestRepository.deleteAll();
        LocalDateTime startDateTime = LocalDateTime.now().plusDays(1);
        testProtestDto = ProtestCreateDto.builder()
                .title("탄핵 시위")
                .description("윤석열 탄핵 찬성")
                .location("여의도")
                .organizer("국민촛불행동")
                .declaredParticipants(100)
                .startDateTime(startDateTime)
                .endDateTime(startDateTime.plusDays(3))
                .build();
    }

    @Test
    @DisplayName("시위 생성")
    void createProtest_WhenStartDateIsInFuture_StatusShouldBeScheduled() {
        // When
        Protest savedProtest = protestService.createProtest(testProtestDto);

        // Then
        assertThat(savedProtest.getId()).isNotNull();
        assertThat(savedProtest.getStatus()).isEqualTo(ProtestStatus.SCHEDULED);
        assertThat(savedProtest.getTitle()).isEqualTo(testProtestDto.getTitle());
    }

    @Test
    @DisplayName("날짜 및 지역별 시위 목록 조회 - 오늘이 아닌 시위는 보여주지 않음. 현재 시간 이전의 STATUS = SCHEDULED")
    void getTodayProtests_scheduled() {}

    @Test
    @DisplayName("날짜 및 지역별 시위 목록 조회 - 오늘이 아닌 시위는 보여주지 않음. 오늘 내에 현재 시간 이후의 STATUS = ONGOING")
    void getTodayProtests_onGoing() {}

    @Test
    @DisplayName("특정 시위 상세 조회")
    void getProtestDetail_scheduled() {
        // Given
        Protest savedProtest = protestService.createProtest(testProtestDto);

        // When
        ProtestDetailDto detail =  protestService.getProtestDetail(savedProtest.getId());

        // Then
        assertThat(detail).isNotNull();
        assertThat(detail.getTitle()).isEqualTo(testProtestDto.getTitle());
        assertThat(detail.getOrganizer()).isEqualTo(testProtestDto.getOrganizer());
        assertThat(detail.getDeclaredParticipants()).isEqualTo(testProtestDto.getDeclaredParticipants());
        assertThat(detail.getStatus()).isEqualTo(ProtestStatus.SCHEDULED);
    }

    @Test
    @DisplayName("성공: 특정 시위 정보 수정")
    void updateProtest() throws ChangeSetPersister.NotFoundException {
        // Given
        Protest savedProtest = protestService.createProtest(testProtestDto);

        // When
        ProtestUpdateDto updateDto = ProtestUpdateDto.builder().
                title("수정된 제목")
                .description("수정된 설명")
                .location("국회")
                .organizer("변경된 주체")
                .declaredParticipants(1000)
                .startDateTime(LocalDateTime.now())
                .build();
        Long id = protestService.updateProtest(savedProtest.getId(), updateDto);
        ProtestDetailDto detail =  protestService.getProtestDetail(id);

        // Then
        assertThat(detail.getTitle()).isEqualTo(updateDto.getTitle());
        assertThat(detail.getDescription()).isEqualTo(updateDto.getDescription());
        assertThat(detail.getLocation()).isEqualTo(updateDto.getLocation());
        assertThat(detail.getDeclaredParticipants()).isEqualTo(updateDto.getDeclaredParticipants());
        assertThat(detail.getOrganizer()).isEqualTo(updateDto.getOrganizer());
        assertThat(detail.getStatus()).isEqualTo(ProtestStatus.SCHEDULED);
        assertThat(detail.getStartDateTime()).isEqualTo(updateDto.getStartDateTime());
    }

    @Test
    @DisplayName("실패: 유효하지 않은 ID 수정 시도") // TODO: 예외처리 방식에 대해 고민 후 작성
    void updateProtest_WhenProtestIsOngoing_ShouldThrowException() {
        // Given
        Long invalidId = 1000L;

        // When
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
