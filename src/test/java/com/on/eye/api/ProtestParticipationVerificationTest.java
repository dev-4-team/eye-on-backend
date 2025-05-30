package com.on.eye.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;

import com.on.eye.api.auth.error.exception.OutOfValidProtestRangeException;
import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.entity.Location;
import com.on.eye.api.protest.dto.ParticipateVerificationRequest;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import com.on.eye.api.protest.error.exception.AbnormalMovementPatternException;
import com.on.eye.api.protest.error.exception.DuplicateVerificationException;
import com.on.eye.api.protest.repository.ProtestRepository;
import com.on.eye.api.protest.service.ProtestService;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProtestParticipationVerificationTest {

    @Autowired private ProtestService protestService;

    private Long testProtestId;
    private final List<Long> createdProtestIds = new ArrayList<>();
    private Location testLocation;
    private LocationDto testLocationDto;
    private ParticipateVerificationRequest testParticipateVerificationRequest;
    private static final Long TEST_USER_ID = 1L;

    @Autowired private ProtestRepository protestRepository;

    @BeforeAll
    void setUp() {
        // 테스트용 시위 및 위치 데이터 생성
        testLocationDto =
                new LocationDto("테스트 장소", new BigDecimal("37.5665"), new BigDecimal("126.9780"));
        // 테스트용 위치 생성
        testLocation =
                Location.builder()
                        .name(testLocationDto.name())
                        .latitude(testLocationDto.latitude())
                        .longitude(testLocationDto.longitude())
                        .build();
        testParticipateVerificationRequest =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude(), testLocation.getLatitude());
        // 테스트용 사용자 인증 컨텍스트 설정
        setUpSecurityContext();
    }

    @BeforeEach
    void beforeEach() {
        testProtestId = createTestProtest(testLocationDto);
    }

    @AfterEach
    void afterEach() {
        cleanUp();
    }

    @AfterAll
    void afterAll() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("유효한 반경 내에서 시위 참여 인증 성공")
    void verifyParticipationWithinRadius() {
        // When
        Boolean result =
                protestService.participateVerify(testProtestId, testParticipateVerificationRequest);

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
        // When & Then
        // 첫 번째 인증
        protestService.participateVerify(testProtestId, testParticipateVerificationRequest);

        // 두 번째 인증 시도
        assertThatThrownBy(
                        () ->
                                protestService.participateVerify(
                                        testProtestId, testParticipateVerificationRequest))
                .isInstanceOf(DuplicateVerificationException.class);
    }

    @Test
    @DisplayName("비정상적인 이동속도 인증 시도 실패")
    void preventTooFastVerification() {
        // Given
        protestService.participateVerify(testProtestId, testParticipateVerificationRequest);

        LocationDto farLocation =
                new LocationDto("많이 먼 시위", new BigDecimal("38.5665"), new BigDecimal("126.9780"));
        Long farProtestId = createTestProtest(farLocation);

        // When
        ParticipateVerificationRequest farRequest =
                new ParticipateVerificationRequest(farLocation.longitude(), farLocation.latitude());

        // Then
        assertThatThrownBy(() -> protestService.participateVerify(farProtestId, farRequest))
                .isInstanceOf(AbnormalMovementPatternException.class);
    }

    private void setUpSecurityContext() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        TEST_USER_ID.toString(),
                        "credentials",
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Long createTestProtest(LocationDto locationDto) {
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
        Long createdProtestId = protestIds.get(0);
        createdProtestIds.add(createdProtestId);
        return createdProtestId;
    }

    void cleanUp() {
        protestRepository.deleteAllById(createdProtestIds);
    }
}
