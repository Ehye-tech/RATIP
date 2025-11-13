package com.ratip.service;

import com.ratip.ai.ChatGptClient;
import com.ratip.model.CorrelatedEvent;
import com.ratip.repository.MockDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryService {
    
    private final MockDataRepository repository;
    private final EventCorrelator correlator;
    private final ChatGptClient chatGptClient;
    
    public String processQuery(String userQuery) {
        try {
            log.info("Processing query: {}", userQuery);
            
            Duration timeRange = extractTimeRange(userQuery);
            Instant endTime = Instant.now();
            Instant startTime = endTime.minus(timeRange);
            
            List<CorrelatedEvent> correlations = repository.getCorrelations(startTime, endTime);
            
            String response = chatGptClient.summarizeEvents(correlations, userQuery);
            
            log.info("Query processed successfully");
            return response;
            
        } catch (Exception e) {
            log.error("Error processing query", e);
            return "Error processing your query: " + e.getMessage();
        }
    }
    
    private Duration extractTimeRange(String query) {
        String lowerQuery = query.toLowerCase();
        
        if (lowerQuery.contains("hour") || lowerQuery.contains("last hour")) {
            return Duration.ofHours(1);
        } else if (lowerQuery.contains("24 hours") || lowerQuery.contains("day")) {
            return Duration.ofDays(1);
        } else if (lowerQuery.contains("week")) {
            return Duration.ofDays(7);
        } else {
            return Duration.ofHours(2);
        }
    }
}
