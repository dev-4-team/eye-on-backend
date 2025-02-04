package com.on.eye.api.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import com.on.eye.api.common.annotation.CollectionValidator;
import com.on.eye.api.domain.Protest;
import com.on.eye.api.dto.*;
import com.on.eye.api.service.ProtestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/protest")
@RequiredArgsConstructor
public class ProtestController {
    private final ProtestService protestService;
    protected final LocalValidatorFactoryBean validator;

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.addValidators(new CollectionValidator(validator));
    }

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
                .body(
                        protestService.createProtest(protestCreateRequest).stream()
                                .map(Protest::getId)
                                .toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProtestResponse> getProtestDetail(@PathVariable Long id) {
        ProtestResponse protestResponse = protestService.getProtestDetail(id);
        if (protestResponse == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(protestResponse);
    }

    @GetMapping
    public ResponseEntity<List<ProtestItemResponse>> getProtestsBy(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<ProtestItemResponse> protests = protestService.getProtestsBy(date);
        return ResponseEntity.ok(protests);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateProtest(
            @PathVariable Long id, @Valid @RequestBody ProtestUpdateDto updateDto) {
        Long updatedId = protestService.updateProtest(id, updateDto);
        return ResponseEntity.ok(updatedId);
    }
}
