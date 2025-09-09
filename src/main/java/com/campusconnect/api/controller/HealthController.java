package com.campusconnect.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", LocalDateTime.now(),
            "service", "CampusConnect API",
            "version", "1.0.0"
        ));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> info() {
        return ResponseEntity.ok(Map.of(
            "application", Map.of(
                "name", "CampusConnect API",
                "description", "Campus delivery and rideshare platform for KNUST students",
                "version", "1.0.0",
                "encoding", "UTF-8",
                "java", Map.of(
                    "version", System.getProperty("java.version"),
                    "vendor", System.getProperty("java.vendor")
                )
            )
        ));
    }
}
