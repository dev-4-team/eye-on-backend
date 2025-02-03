package com.on.eye.api.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.*;
import com.on.eye.api.service.ProtestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/protest")
@RequiredArgsConstructor
public class ProtestController {
    private final ProtestService protestService;

    @PostMapping("{id}/participate/verify")
    public ResponseEntity<Boolean> participateVerify(
            @PathVariable Long id, @Valid @RequestBody ParticipateVerificationRequest request) {
        // 냅다 ok가 아니라, 성공이면 ok, 실패면 success = false. 거리가 멀다 이런 식의 메세지가 나갔으면 함. 실패 사유가 나갔으면.
        return ResponseEntity.ok(protestService.participateVerify(id, request));
    }

    @PostMapping
    public ResponseEntity<List<Long>> createProtest(
            @Valid @RequestBody List<ProtestCreateDto> protestCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        protestService.createProtest(protestCreateDto).stream()
                                .map(Protest::getId)
                                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProtestDetailDto> getProtestDetail(@PathVariable Long id) {
        ProtestDetailDto protestDetailDto = protestService.getProtestDetail(id);
        if (protestDetailDto == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(protestDetailDto);
    }

    @GetMapping
    public ResponseEntity<List<ProtestListItemDto>> getProtestsBy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<ProtestListItemDto> protests = protestService.getProtestsBy(date);
        return ResponseEntity.ok(protests);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateProtest(
            @PathVariable Long id, @Valid @RequestBody ProtestUpdateDto updateDto) {
        Long updatedId = protestService.updateProtest(id, updateDto);
        return ResponseEntity.ok(updatedId);
    }
}
