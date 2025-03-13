package com.on.eye.api.protest.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.on.eye.api.protest.dto.*;
import com.on.eye.api.protest.service.ProtestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/protest")
@RequiredArgsConstructor
@Validated
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
            @Valid @RequestBody List<ProtestCreateRequest> protestCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(protestService.createProtest(protestCreateRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProtestResponse> getProtestDetail(@PathVariable Long id) {
        ProtestResponse protestResponse = protestService.getProtestDetail(id);
        if (protestResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(protestResponse);
    }

    @GetMapping("/verifications")
    public ResponseEntity<List<ProtestVerificationResponse>> getVerificationsNum(
            @RequestParam(required = false) Long protestId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        if (date == null) date = LocalDate.now();
        List<ProtestVerificationResponse> response =
                protestService.getProtestVerifications(protestId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ProtestResponse>> getProtestsBy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<ProtestResponse> protests = protestService.getProtestsBy(date);
        return ResponseEntity.ok(protests);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateProtest(
            @PathVariable Long id, @Valid @RequestBody ProtestUpdateDto updateDto) {
        Long updatedId = protestService.updateProtest(id, updateDto);
        return ResponseEntity.ok(updatedId);
    }
}
