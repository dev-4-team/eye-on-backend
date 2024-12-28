package com.on.eye.api.demoInfo.controller;

import com.on.eye.api.demoInfo.service.DemoImageCrawlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping()
@RequiredArgsConstructor
public class DemoInfoController {

    private final DemoImageCrawlerService demoImageCrawlerService;

    @GetMapping("/image")
    public ResponseEntity<?> crawlImage() {
        demoImageCrawlerService.crawlPosts();
        return ResponseEntity.ok().build();
    }
}