package com.on.eye.api;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.domain.ProtestStatus;
import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestDetailDto;
import com.on.eye.api.dto.ProtestListItemDto;
import com.on.eye.api.dto.ProtestUpdateDto;
import com.on.eye.api.exception.ResourceNotFoundException;
import com.on.eye.api.repository.ProtestRepository;
import com.on.eye.api.service.ProtestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class ProtestServiceTest {
    @Autowired
    private ProtestService protestService;

    @Autowired
    private ProtestRepository protestRepository;

    private ProtestCreateDto testProtestDto;

    // Test Fixture Pattern
    static class TestProtestFixture {
        static ProtestCreateDto.ProtestCreateDtoBuilder baseBuilder() {
            LocalDateTime startDateTime = LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0));
            return ProtestCreateDto.builder()
                    .title("탄핵 시위")
                    .description("윤석열 탄핵 찬성")
                    .location("여의도")
                    .organizer("국민촛불행동")
                    .declaredParticipants(100)
                    .startDateTime(startDateTime)
                    .endDateTime(startDateTime.plusHours(3));
        }

        static ProtestCreateDto createDefault() {
            return baseBuilder().build();
        }

        static ProtestCreateDto createWithTitle(String title) {
            return baseBuilder().title(title).build();
        }

        static ProtestCreateDto createWithStartDateTime(LocalDateTime startDateTime) {
            return baseBuilder()
                    .startDateTime(startDateTime)
                    .endDateTime(startDateTime.plusHours(3))
                    .build();
        }

        static List<ProtestCreateDto> createMultipleProtests(int count) {
            return IntStream.range(0, count)
                    .mapToObj(i -> baseBuilder()
                            .title("시위 " + (i + 1))
                            .location("장소 " + (i + 1))
                            .build())
                    .toList();
        }
    }

    @BeforeEach
    void setUp() {
        protestRepository.deleteAll();
        testProtestDto = TestProtestFixture.createDefault();
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
    void updateProtest() {
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
    @DisplayName("실패: 유효하지 않은 ID 수정 시도")
    void updateProtest_WhenProtestIsOngoing_ShouldThrowException() {
        // Given
        Long invalidId = 1000L;

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> protestService.updateProtest(invalidId, null));
        assertThrows(ResourceNotFoundException.class, () -> protestService.getProtestDetail(invalidId));
    }

    @Nested
    @DisplayName("시위 목록 테스트")
    class ProtestListTests {
        private List<Protest> testProtests;

        @BeforeEach
        void setup() {
            protestRepository.deleteAll();
            testProtests = TestProtestFixture.createMultipleProtests(5)
                    .stream()
                    .map(protestService::createProtest)
                    .toList();
        }

        @Test
        @DisplayName("성공: 날짜 기준 시위 목록 조회 - 오늘")
        void getProtestsByDate() {
            // Given
            LocalDateTime twoDaysAfter = LocalDateTime.of(LocalDate.now().plusDays(2), LocalTime.of(10, 0));
            ProtestCreateDto diffDto = TestProtestFixture.createWithStartDateTime(twoDaysAfter);
            protestService.createProtest(diffDto);

            // When
            LocalDate day = LocalDate.now().plusDays(1);
            List<ProtestListItemDto> protests = protestService.getProtestsBy(day);

            // Then
            assertThat(protests).hasSize(testProtests.size());
        }
    }
}
