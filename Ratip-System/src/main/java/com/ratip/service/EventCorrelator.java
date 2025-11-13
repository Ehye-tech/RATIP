package com.ratip.service;

import com.ratip.model.AlarmEvent;
import com.ratip.model.CorrelatedEvent;
import com.ratip.model.TelemetryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class EventCorrelator {
    
    public List<CorrelatedEvent> correlateEvents(List<TelemetryEvent> telemetryEvents, List<AlarmEvent> alarms) {
        List<CorrelatedEvent> correlations = new ArrayList<>();
        
        for (AlarmEvent alarm : alarms) {
            for (TelemetryEvent telemetry : telemetryEvents) {
                if (isCorrelated(alarm, telemetry)) {
                    CorrelatedEvent correlation = buildCorrelation(alarm, telemetry);
                    correlations.add(correlation);
                    log.debug("Created correlation: {} (confidence: {})", 
                            correlation.getDescription(), correlation.getConfidenceScore());
                }
            }
        }
        
        return correlations;
    }
    
    private boolean isCorrelated(AlarmEvent alarm, TelemetryEvent telemetry) {
        if (!alarm.getServiceName().equals(telemetry.getServiceName())) {
            return false;
        }
        
        if (!alarm.getMetricType().equals(telemetry.getMetricType())) {
            return false;
        }
        
        Duration timeDiff = Duration.between(telemetry.getTimestamp(), alarm.getTimestamp());
        return Math.abs(timeDiff.toMinutes()) <= 5;
    }
    
    private CorrelatedEvent buildCorrelation(AlarmEvent alarm, TelemetryEvent telemetry) {
        double confidence = calculateConfidence(alarm, telemetry);
        
        return CorrelatedEvent.builder()
                .id(UUID.randomUUID().toString())
                .correlationType("Metric-Alarm Correlation")
                .confidenceScore(confidence)
                .description(String.format("%s metric anomaly triggered %s alarm", 
                        telemetry.getMetricType(), alarm.getAlarmName()))
                .correlationTimestamp(Instant.now())
                .alarm(alarm)
                .telemetry(telemetry)
                .rootCause(determineRootCause(alarm, telemetry))
                .recommendedAction(recommendAction(alarm, telemetry))
                .build();
    }
    
    private double calculateConfidence(AlarmEvent alarm, TelemetryEvent telemetry) {
        double baseConfidence = 0.7;
        
        if (alarm.getSeverity().equals("CRITICAL")) {
            baseConfidence += 0.2;
        }
        
        Duration timeDiff = Duration.between(telemetry.getTimestamp(), alarm.getTimestamp());
        if (Math.abs(timeDiff.toMinutes()) <= 2) {
            baseConfidence += 0.1;
        }
        
        return Math.min(baseConfidence, 1.0);
    }
    
    private String determineRootCause(AlarmEvent alarm, TelemetryEvent telemetry) {
        return String.format("Service %s experienced %s exceeding threshold of %.2f with value %.2f",
                alarm.getServiceName(), alarm.getMetricType(), alarm.getThreshold(), telemetry.getValue());
    }
    
    private String recommendAction(AlarmEvent alarm, TelemetryEvent telemetry) {
        if (alarm.getMetricType().contains("Latency")) {
            return "Investigate service dependencies and database query performance";
        } else if (alarm.getMetricType().contains("Error")) {
            return "Review application logs and check for recent deployments";
        } else if (alarm.getMetricType().contains("CPU") || alarm.getMetricType().contains("Memory")) {
            return "Scale up service capacity or optimize resource usage";
        }
        return "Monitor the situation and investigate if pattern persists";
    }
}
