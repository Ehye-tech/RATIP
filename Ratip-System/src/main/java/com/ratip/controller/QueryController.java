package com.ratip.controller;

import com.ratip.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class QueryController {
    
    private final QueryService queryService;
    
    @PostMapping("/query")
    public ResponseEntity<Map<String, String>> handleQuery(@RequestBody Map<String, String> request) {
        try {
            String userQuery = request.get("query");
            
            if (userQuery == null || userQuery.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Query cannot be empty"));
            }
            
            log.info("Received query: {}", userQuery);
            
            String response = queryService.processQuery(userQuery);
            
            return ResponseEntity.ok(Map.of(
                    "query", userQuery,
                    "response", response,
                    "timestamp", Instant.now().toString()
            ));
            
        } catch (Exception e) {
            log.error("Error handling query", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "RATIP",
                "timestamp", Instant.now().toString()
        ));
    }
}
