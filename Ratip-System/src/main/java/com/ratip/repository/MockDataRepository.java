package com.ratip.repository;

import com.ratip.model.AlarmEvent;
import com.ratip.model.CorrelatedEvent;
import com.ratip.model.TelemetryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class MockDataRepository {
    
    private final ConcurrentHashMap<String, TelemetryEvent> telemetryStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AlarmEvent> alarmStore = new ConcurrentHashMap<>();
    
    public void saveTelemetry(TelemetryEvent event) {
        telemetryStore.put(event.getId(), event);
        log.debug("Saved telemetry: {}", event.getId());
    }
    
    public void saveAlarm(AlarmEvent event) {
        alarmStore.put(event.getId(), event);
        log.debug("Saved alarm: {}", event.getId());
    }
    
    public List<TelemetryEvent> getTelemetryByServiceAndTime(String serviceName, String metricType, Instant start, Instant end) {
        return telemetryStore.values().stream()
                .filter(event -> event.getServiceName().equals(serviceName))
                .filter(event -> event.getMetricType().equals(metricType))
                .filter(event -> event.getTimestamp().isAfter(start) && event.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }
    
    public List<AlarmEvent> getAlarmsByServiceAndTime(String serviceName, String severity, Instant start, Instant end) {
        return alarmStore.values().stream()
                .filter(event -> event.getServiceName().equals(serviceName))
                .filter(event -> severity == null || event.getSeverity().equals(severity))
                .filter(event -> event.getTimestamp().isAfter(start) && event.getTimestamp().isBefore(end))
                .collect(Collectors.toList());
    }
    
    public List<CorrelatedEvent> getCorrelations(Instant start, Instant end) {
        List<CorrelatedEvent> correlations = new ArrayList<>();
        
        String[] correlationPatterns = {
            "Lambda cold starts correlated with API latency spikes",
            "DynamoDB throttling events preceding error rate increase",
            "ECS memory pressure during peak traffic hours",
            "Kinesis lag causing downstream Lambda timeouts",
            "API Gateway timeout linked to backend service degradation"
        };
        
        for (int i = 0; i < correlationPatterns.length; i++) {
            Instant timestamp = end.minus(Duration.ofHours(i));
            
            TelemetryEvent telemetry = TelemetryEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .serviceName("api-gateway")
                    .metricType("API_Latency")
                    .value(150.0 + Math.random() * 100)
                    .timestamp(timestamp)
                    .region("us-east-1")
                    .environment("production")
                    .build();
            
            AlarmEvent alarm = AlarmEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .alarmName("High Latency Alarm")
                    .serviceName("api-gateway")
                    .metricType("API_Latency")
                    .severity(i % 2 == 0 ? "CRITICAL" : "WARNING")
                    .state("ALARM")
                    .threshold(200.0)
                    .value(telemetry.getValue())
                    .timestamp(timestamp.plus(Duration.ofMinutes(1)))
                    .description("API latency exceeded threshold")
                    .region("us-east-1")
                    .build();
            
            CorrelatedEvent correlation = CorrelatedEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .correlationType("Metric-Alarm Correlation")
                    .confidenceScore(0.70 + Math.random() * 0.30)
                    .description(correlationPatterns[i])
                    .correlationTimestamp(timestamp)
                    .alarm(alarm)
                    .telemetry(telemetry)
                    .rootCause("Service experiencing increased load")
                    .recommendedAction("Scale up service capacity")
                    .build();
            
            correlations.add(correlation);
        }
        
        return correlations;
    }
    
    public List<TelemetryEvent> getAllTelemetry() {
        return new ArrayList<>(telemetryStore.values());
    }
    
    public List<AlarmEvent> getAllAlarms() {
        return new ArrayList<>(alarmStore.values());
    }
}
