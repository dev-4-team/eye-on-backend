package com.on.eye.api.protest.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.on.eye.api.protest.dto.Coordinate;
import com.on.eye.api.protest.dto.ProtestCreateRequest;
import com.on.eye.api.protest.dto.ProtestResponse;
import com.on.eye.api.protest.service.ProtestFacade;
import com.on.eye.api.protest.service.ProtestService;
import com.on.eye.api.protest_verification.dto.ProtestVerificationResponse;
import com.on.eye.api.protest_verification.service.ProtestVerificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/protest")
@RequiredArgsConstructor
@Validated
public class ProtestController {
    private final ProtestService protestService;
    private final ProtestFacade protestFacade;
    private final ProtestVerificationService protestVerificationService;

    @PostMapping("{id}/participate/verify")
    public ResponseEntity<Boolean> participateVerify(
            @PathVariable Long id, @Valid @RequestBody Coordinate request) {

        return ResponseEntity.ok(protestFacade.participateVerify(id, request));
    }

    @PostMapping
    public ResponseEntity<List<Long>> createProtest(
            @Valid @RequestBody List<ProtestCreateRequest> protestCreateRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(protestFacade.createProtest(protestCreateRequest));
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
                protestVerificationService.getTodayProtestVerifications(protestId, date);
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
}
