package com.on.eye.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.on.eye.api.domain.Location;
import com.on.eye.api.domain.ParticipantsVerification;
import com.on.eye.api.dto.LocationDto;
import com.on.eye.api.dto.ParticipateVerificationRequest;
import com.on.eye.api.dto.ProtestCreateRequest;
import com.on.eye.api.exception.DuplicateVerificationException;
import com.on.eye.api.exception.OutOfValidProtestRangeException;
import com.on.eye.api.repository.LocationRepository;
import com.on.eye.api.repository.ParticipantVerificationRepository;
import com.on.eye.api.repository.ProtestVerificationRepository;
import com.on.eye.api.service.ProtestService;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProtestParticipationVerificationTest {

    private static final Logger log =
            LoggerFactory.getLogger(ProtestParticipationVerificationTest.class);
    @Autowired private ProtestService protestService;

    @Autowired private LocationRepository locationRepository;
    @Autowired private ParticipantVerificationRepository participantVerificationRepository;

    private Long testProtestId;
    private Location testLocation;
    private static final Long TEST_USER_ID = 1L;
    private final AtomicBoolean cleanUpExecuted = new AtomicBoolean(false);

    @Autowired private ProtestVerificationRepository protestVerificationRepository;

    @BeforeAll
    void setUp() {
        // 테스트용 사용자 인증 컨텍스트 설정
        setUpSecurityContext();
    }

    @BeforeEach
    void beforeEach() {
        // 테스트용 시위 및 위치 데이터 생성
        createTestProtest();
    }

    @AfterEach
    void afterEach() {
        cleanUp();
    }

    @AfterAll
    void afterAll() {
        performCleanup();
    }

    @Test
    @DisplayName("유효한 반경 내에서 시위 참여 인증 성공")
    void verifyParticipationWithinRadius() {
        // Given
        ParticipateVerificationRequest request =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude(), // 정확히 같은 위치
                        testLocation.getLatitude());

        // When
        Boolean result = protestService.participateVerify(testProtestId, request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("유효 반경을 벗어난 위치에서 시위 참여 인증 실패")
    void verifyParticipationOutsideRadius() {
        // Given
        ParticipateVerificationRequest request =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude().add(new BigDecimal("0.1")), // 약 11km 떨어진 위치,
                        testLocation.getLatitude());

        // When & Then
        assertThatThrownBy(() -> protestService.participateVerify(testProtestId, request))
                .isInstanceOf(OutOfValidProtestRangeException.class);
    }

    @Test
    @DisplayName("동일 시위에 대한 중복 인증 시도 실패")
    void preventDuplicateVerification() {
        // Given
        ParticipateVerificationRequest request =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude(), testLocation.getLatitude());

        // When & Then
        // 첫 번째 인증
        protestService.participateVerify(testProtestId, request);

        // 두 번째 인증 시도
        assertThatThrownBy(() -> protestService.participateVerify(testProtestId, request))
                .isInstanceOf(DuplicateVerificationException.class);
    }

    private void setUpSecurityContext() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        TEST_USER_ID.toString(),
                        "credentials",
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void createTestProtest() {
        // 테스트용 위치 생성
        LocationDto locationDto =
                new LocationDto("테스트 장소", new BigDecimal("37.5665"), new BigDecimal("126.9780"));

        testLocation =
                Location.builder()
                        .name(locationDto.name())
                        .latitude(locationDto.latitude())
                        .longitude(locationDto.longitude())
                        .build();

        // 테스트용 시위 생성
        ProtestCreateRequest protestRequest =
                ProtestCreateRequest.builder()
                        .title("테스트 시위")
                        .startDateTime(LocalDateTime.now().plusHours(1))
                        .endDateTime(LocalDateTime.now().plusHours(3))
                        .organizer("테스트 단체")
                        .declaredParticipants(100)
                        .locations(List.of(locationDto))
                        .build();

        List<Long> protestIds = protestService.createProtest(List.of(protestRequest));
        testProtestId = protestIds.get(0);
    }

    void cleanUp() {
        log.info("Cleaning up...");
        List<ParticipantsVerification> verifications =
                participantVerificationRepository.getParticipantsVerificationByProtest_Id(
                        testProtestId);
        participantVerificationRepository.deleteAll(verifications);

        locationRepository.delete(testLocation);
        log.info("Cleanup complete.");
    }

    private void performCleanup() {
        if (cleanUpExecuted.compareAndSet(false, true)) {
            try {
                log.info("Executing cleanup...");
                cleanUp();
                cleanUpExecuted.set(true);
                SecurityContextHolder.clearContext();
                log.info("Cleanup complete.");
            } catch (Exception e) {
                log.error("Cleanup failed.", e);
                throw e;
            }
        }
    }
}
