package com.on.eye.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
import com.on.eye.api.repository.ProtestRepository;
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

    @Autowired private ProtestRepository protestRepository;

    @Autowired private LocationRepository locationRepository;
    @Autowired private ParticipantVerificationRepository participantVerificationRepository;

    private Long testProtestId;
    private Location testLocation;
    private static final Long TEST_USER_ID = 1L;
    private List<Long> testUserIds;
    private final int CONCURRENT_USERS = 100000;
    private final int THREADS = 10;
    private ExecutorService executorService;
    private final AtomicBoolean cleanUpExecuted = new AtomicBoolean(false);

    private static final int WARMUP_USERS = 10;
    private static final int TEST_USERS = 150000;
    private static final int THREAD_POOL_SIZE = 200;
    private static final Duration EXPECTED_RESPONSE_TIME = Duration.ofMillis(500);

    private List<Duration> responseTimes;
    private List<String> performanceIssues;
    @Autowired private ProtestVerificationRepository protestVerificationRepository;

    @BeforeAll
    void setUp() {
        // shutdown hook
        Runtime.getRuntime()
                .addShutdownHook(
                        new Thread(
                                () -> {
                                    if (!cleanUpExecuted.get()) {
                                        performCleanup();
                                    }
                                }));

        // 테스트용 사용자 인증 컨텍스트 설정
        setUpSecurityContext();
        createTestUsers();
        executorService = Executors.newFixedThreadPool(THREADS);

        responseTimes = new CopyOnWriteArrayList<>();
        performanceIssues = new CopyOnWriteArrayList<>();
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
        executorService.shutdown();
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

    @Test
    @DisplayName("동시에 여러 사용자가 시위 참여 인증 시도")
    void concurrentParticipationVerification() throws InterruptedException {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(CONCURRENT_USERS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        List<Future<?>> futures = new ArrayList<>();

        ParticipateVerificationRequest request =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude(), testLocation.getLatitude());

        // When
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final Long userId = testUserIds.get(i);
            futures.add(
                    executorService.submit(
                            () -> {
                                try {
                                    startLatch.await(); // 모든 스레드가 동시에 시작하도록 대기
                                    setSecurityContext(userId);

                                    protestService.participateVerify(testProtestId, request);
                                    successCount.incrementAndGet();
                                } catch (DuplicateVerificationException e) {
                                    duplicateCount.incrementAndGet();
                                } catch (Exception e) {
                                    log.error("Unexpected error during verification", e);
                                } finally {
                                    SecurityContextHolder.clearContext();
                                    completionLatch.countDown();
                                }
                            }));
        }

        // 모든 스레드 동시 시작
        startLatch.countDown();

        // 모든 작업 완료 대기
        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        List<ParticipantsVerification> verifications =
                participantVerificationRepository.getParticipantsVerificationByProtest_Id(
                        testProtestId);

        // Then
        assertThat(completed).isTrue();

        assertThat(successCount.get()).isEqualTo(CONCURRENT_USERS);

        assertThat(duplicateCount.get()).isZero();

        // 모든 Future 완료 확인
        for (Future<?> future : futures) {
            assertThat(future.isDone()).isTrue();
        }
    }

    @Test
    @DisplayName("동일 사용자가 여러 스레드에서 동시에 인증 시도")
    void concurrentParticipationVerificationSameUser() throws InterruptedException {
        // Given
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        final Long userId = testUserIds.get(0);

        ParticipateVerificationRequest request =
                new ParticipateVerificationRequest(
                        testLocation.getLongitude(), testLocation.getLatitude());

        // When
        for (int i = 0; i < THREADS; i++) {
            executorService.submit(
                    () -> {
                        try {
                            startLatch.await();
                            setSecurityContext(userId);

                            protestService.participateVerify(testProtestId, request);
                            successCount.incrementAndGet();
                        } catch (DuplicateVerificationException e) {
                            duplicateCount.incrementAndGet();
                        } catch (Exception e) {
                            log.error("Unexpected error during verification", e);
                        } finally {
                            SecurityContextHolder.clearContext();
                            completionLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        completionLatch.await(30, TimeUnit.SECONDS);

        // Then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(duplicateCount.get()).isEqualTo(THREADS - 1);
    }

    @Test
    @Order(1)
    @DisplayName("시스템 웜업")
    void warmupSystem() throws InterruptedException {
        runConcurrentRequests(WARMUP_USERS);
        // 웜업 데이터는 성능 측정에서 제외
        responseTimes.clear();
    }

    @Test
    @Order(2)
    @DisplayName("동시 사용자 처리 성능 테스트")
    void concurrentUsersPerformanceTest() throws InterruptedException {
        // When
        Instant testStart = Instant.now();
        runConcurrentRequests(TEST_USERS);
        Duration totalTestTime = Duration.between(testStart, Instant.now());

        // Then
        analyzePerformance(totalTestTime);

        // verifed_num 확인. 이거 안같은듯?
        Integer verifiedNum =
                protestVerificationRepository.findByProtestId(testProtestId).getVerifiedNum();
        assertThat(verifiedNum).isEqualTo(TEST_USERS);
    }

    private void runConcurrentRequests(int userCount) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(userCount);
        AtomicInteger activeThreads = new AtomicInteger(0);

        // Submit tasks
        for (int i = 0; i < userCount; i++) {
            final Long userId = (long) i;
            executorService.submit(
                    () -> {
                        try {
                            startLatch.await();
                            activeThreads.incrementAndGet();

                            Instant start = Instant.now();
                            executeVerification(userId);
                            Duration responseTime = Duration.between(start, Instant.now());
                            responseTimes.add(responseTime);

                            // 스레드 풀 포화 상태 체크
                            checkThreadPoolSaturation(activeThreads.get());

                        } catch (Exception e) {
                            log.error("Error during verification: ", e);
                            performanceIssues.add("Error in request: " + e.getMessage());
                        } finally {
                            activeThreads.decrementAndGet();
                            completionLatch.countDown();
                        }
                    });
        }

        startLatch.countDown();
        completionLatch.await(30, TimeUnit.SECONDS);
    }

    private void executeVerification(Long userId) {
        setSecurityContext(userId);
        try {
            ParticipateVerificationRequest request =
                    new ParticipateVerificationRequest(
                            testLocation.getLongitude(), testLocation.getLatitude());
            protestService.participateVerify(testProtestId, request);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void checkThreadPoolSaturation(int currentActiveThreads) {
        if (currentActiveThreads >= THREAD_POOL_SIZE) {
            performanceIssues.add(
                    String.format(
                            "Thread pool saturation detected: %d active threads",
                            currentActiveThreads));
        }
    }

    private void analyzePerformance(Duration totalTestTime) {
        if (responseTimes.isEmpty()) {
            fail("No response times recorded");
        }

        // 응답 시간 통계 계산
        Duration maxResponseTime =
                responseTimes.stream().max(Duration::compareTo).orElse(Duration.ZERO);
        Duration minResponseTime =
                responseTimes.stream().min(Duration::compareTo).orElse(Duration.ZERO);
        Duration avgResponseTime = calculateAverageResponseTime();
        Duration p95ResponseTime = calculatePercentileResponseTime(95);
        Duration p99ResponseTime = calculatePercentileResponseTime(99);

        // 성능 메트릭 로깅
        log.info("Performance Test Results:");
        log.info("Total Test Time: {} ms", totalTestTime.toMillis());
        log.info("Average Response Time: {} ms", avgResponseTime.toMillis());
        log.info("95th Percentile Response Time: {} ms", p95ResponseTime.toMillis());
        log.info("99th Percentile Response Time: {} ms", p99ResponseTime.toMillis());
        log.info("Min Response Time: {} ms", minResponseTime.toMillis());
        log.info("Max Response Time: {} ms", maxResponseTime.toMillis());

        if (!performanceIssues.isEmpty()) {
            log.warn("Performance Issues Detected:");
            performanceIssues.forEach(log::warn);
        }

        // Assertions
        assertAll(
                () ->
                        assertTrue(
                                avgResponseTime.compareTo(EXPECTED_RESPONSE_TIME) <= 0,
                                "Average response time exceeds expected time"),
                () ->
                        assertTrue(
                                p95ResponseTime.compareTo(EXPECTED_RESPONSE_TIME.multipliedBy(2))
                                        <= 0,
                                "95th percentile response time is too high"),
                () ->
                        assertTrue(
                                maxResponseTime.compareTo(EXPECTED_RESPONSE_TIME.multipliedBy(5))
                                        <= 0,
                                "Maximum response time is too high"),
                () ->
                        assertTrue(
                                performanceIssues.isEmpty(),
                                "Performance issues detected: "
                                        + String.join(", ", performanceIssues)));
    }

    private Duration calculateAverageResponseTime() {
        long totalMillis = responseTimes.stream().mapToLong(Duration::toMillis).sum();
        return Duration.ofMillis(totalMillis / responseTimes.size());
    }

    private Duration calculatePercentileResponseTime(double percentile) {
        List<Duration> sortedTimes = new ArrayList<>(responseTimes);
        sortedTimes.sort(Duration::compareTo);
        int index = (int) Math.ceil(percentile / 100.0 * sortedTimes.size()) - 1;
        return sortedTimes.get(index);
    }

    private void setSecurityContext(Long userId) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId.toString(),
                        "credentials",
                        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
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

    private void createTestUsers() {
        testUserIds = new ArrayList<>();
        for (long i = 1; i <= CONCURRENT_USERS; i++) {
            testUserIds.add(i);
        }
    }

    void cleanUp() {
        log.info("Cleaning up...");
        List<ParticipantsVerification> verifications =
                participantVerificationRepository.getParticipantsVerificationByProtest_Id(
                        testProtestId);
        participantVerificationRepository.deleteAll(verifications);

        //        protestRepository.deleteById(testProtestId);
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
