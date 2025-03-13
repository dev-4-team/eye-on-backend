package com.on.eye.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.validation.ConstraintViolationException;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.on.eye.api.location.dto.LocationDto;
import com.on.eye.api.location.entity.Location;
import com.on.eye.api.location.repository.LocationRepository;
import com.on.eye.api.organizer.entity.Organizer;
import com.on.eye.api.organizer.repository.OrganizerRepository;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import com.on.eye.api.protest.dto.ProtestResponse;
import com.on.eye.api.protest.entity.Protest;
import com.on.eye.api.protest.repository.ProtestRepository;
import com.on.eye.api.protest.service.ProtestService;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProtestCreateTest {

    @Autowired private ProtestService protestService;

    @Autowired private ProtestRepository protestRepository;

    @Autowired private LocationRepository locationRepository;

    @Autowired private OrganizerRepository organizerRepository;

    private List<Long> createdProtestIds;
    private List<Location> createdLocations;

    @BeforeAll
    void setUp() {
        createdProtestIds = new ArrayList<>();
        createdLocations = new ArrayList<>();
    }

    @AfterEach
    void cleanUp() {
        protestRepository.deleteAllById(createdProtestIds);
        locationRepository.deleteAll(createdLocations);
    }

    @Test
    @DisplayName("정상적인 시위 생성 테스트")
    void createValidProtest() {
        // Given
        LocationDto locationDto = createValidLocationDto("서울시청", "37.566", "126.978");
        ProtestCreateRequest request =
                createValidProtestRequest(
                        "평화로운 시위",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "시민단체",
                        100,
                        List.of(locationDto));

        // When
        List<Long> protestIds = protestService.createProtest(List.of(request));
        createdProtestIds.addAll(protestIds);

        // Then
        assertThat(protestIds).hasSize(1);
        Protest created = protestRepository.findById(protestIds.get(0)).orElseThrow();
        assertThat(created.getTitle()).isEqualTo("평화로운 시위");
        assertThat(created.getDeclaredParticipants()).isEqualTo(100);
    }

    @Test
    @DisplayName("시위 주체명 및 타이틀 테스트")
    void validProtestOrganizerAndTitle() {
        // Given
        String organizerName = "국민촛불행동";
        String organizerTitle = "평화로운 시위";
        String organizerDesc = "촛불 시위입니다.";
        organizerRepository
                .findOrganizerByName(organizerName)
                .ifPresentOrElse(
                        organizer -> {},
                        () ->
                                organizerRepository.save(
                                        Organizer.builder()
                                                .name(organizerName)
                                                .title(organizerTitle)
                                                .description(organizerDesc)
                                                .build()));

        LocationDto locationDto = createValidLocationDto("서울시청", "37.566", "126.978");
        ProtestCreateRequest request =
                createValidProtestRequest(
                        "평화로운 시위",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "촛불행동",
                        100,
                        List.of(locationDto));

        // When
        List<Long> protestIds = protestService.createProtest(List.of(request));
        createdProtestIds.addAll(protestIds);

        // Then
        ProtestResponse protestDetail = protestService.getProtestDetail(protestIds.get(0));
        assertThat(protestDetail.getOrganizer()).isEqualTo(organizerName);
        assertThat(protestDetail.getTitle()).isEqualTo(organizerTitle);
        assertThat(protestDetail.getDescription()).isEqualTo(organizerDesc);
    }

    @Test
    @DisplayName("참가자 수 범위 초과 시 실패")
    void createProtestWithInvalidParticipants() {
        // Given
        LocationDto locationDto = createValidLocationDto("서울시청", "37.566", "126.978");
        ProtestCreateRequest request =
                createValidProtestRequest(
                        "대규모 시위",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "시민단체",
                        5000001, // 최대 허용치 초과
                        List.of(locationDto));

        // When & Then
        List<ProtestCreateRequest> requests = List.of(request);
        assertThatThrownBy(() -> protestService.createProtest(requests))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("동일 위치에 대한 중복 생성 방지 테스트")
    void preventDuplicateLocation() {
        // Given
        LocationDto locationDto = createValidLocationDto("서울시청", "37.566", "126.978");
        ProtestCreateRequest request1 =
                createValidProtestRequest(
                        "첫 번째 시위",
                        LocalDateTime.now().plusDays(1),
                        LocalDateTime.now().plusDays(1).plusHours(2),
                        "시민단체1",
                        100,
                        List.of(locationDto));

        ProtestCreateRequest request2 =
                createValidProtestRequest(
                        "두 번째 시위",
                        LocalDateTime.now().plusDays(2),
                        LocalDateTime.now().plusDays(2).plusHours(2),
                        "시민단체2",
                        200,
                        List.of(locationDto));

        // When
        List<Long> protestIds1 = protestService.createProtest(List.of(request1));
        List<Long> protestIds2 = protestService.createProtest(List.of(request2));
        createdProtestIds.addAll(protestIds1);
        createdProtestIds.addAll(protestIds2);

        // Then
        Location location = locationRepository.findByName("서울시청").orElseThrow();
        assertThat(locationRepository.findAll())
                .filteredOn(loc -> loc.getName().equals(location.getName()))
                .hasSize(1); // 동일 위치는 1개만 존재해야 함
    }

    private LocationDto createValidLocationDto(String name, String latitude, String longitude) {
        return new LocationDto(name, new BigDecimal(latitude), new BigDecimal(longitude));
    }

    private ProtestCreateRequest createValidProtestRequest(
            String title,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime,
            String organizer,
            Integer declaredParticipants,
            List<LocationDto> locations) {
        return ProtestCreateRequest.builder()
                .title(title)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .organizer(organizer)
                .declaredParticipants(declaredParticipants)
                .locations(locations)
                .build();
    }
}
