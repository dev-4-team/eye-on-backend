package com.on.eye.api.controller;

import com.on.eye.api.dto.ProtestCreateDto;
import com.on.eye.api.dto.ProtestDetailDto;
import com.on.eye.api.dto.ProtestListItemDto;
import com.on.eye.api.dto.ProtestUpdateDto;
import com.on.eye.api.service.ProtestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/protest")
@RequiredArgsConstructor
public class ProtestController {
    private final ProtestService protestService;

    @PostMapping
    public ResponseEntity<Long> createProtest(@Valid @RequestBody ProtestCreateDto protestCreateDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(protestService.createProtest(protestCreateDto).getId());
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
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        List<ProtestListItemDto> protests = protestService.getProtestsBy(date);
        return ResponseEntity.ok(protests);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Long> updateProtest(
            @PathVariable Long id,
            @Valid @RequestBody ProtestUpdateDto updateDto
    ) {
        Long updatedId = protestService.updateProtest(id, updateDto);
        return ResponseEntity.ok(updatedId);
    }
}
