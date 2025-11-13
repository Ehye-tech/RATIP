package com.ratip.service;

import com.ratip.model.CorrelatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {
    
    public void sendNotification(CorrelatedEvent correlation) {
        try {
            String message = buildNotificationMessage(correlation);
            
            log.info("📧 Notification sent for correlation: {} (Confidence: {}%)", 
                    correlation.getDescription(), 
                    String.format("%.1f", correlation.getConfidenceScore() * 100));
            
            log.debug("Notification message:\n{}", message);
            
        } catch (Exception e) {
            log.error("Error sending notification", e);
        }
    }
    
    private String buildNotificationMessage(CorrelatedEvent correlation) {
        StringBuilder sb = new StringBuilder();
        sb.append("RATIP Correlation Alert\n\n");
        sb.append("Type: ").append(correlation.getCorrelationType()).append("\n");
        sb.append("Confidence: ").append(String.format("%.1f%%", correlation.getConfidenceScore() * 100)).append("\n");
        sb.append("Description: ").append(correlation.getDescription()).append("\n\n");
        
        if (correlation.getAlarm() != null) {
            sb.append("Alarm Details:\n");
            sb.append("  - Name: ").append(correlation.getAlarm().getAlarmName()).append("\n");
            sb.append("  - Service: ").append(correlation.getAlarm().getServiceName()).append("\n");
            sb.append("  - Severity: ").append(correlation.getAlarm().getSeverity()).append("\n");
        }
        
        if (correlation.getTelemetry() != null) {
            sb.append("\nTelemetry Context:\n");
            sb.append("  - Metric: ").append(correlation.getTelemetry().getMetricType()).append("\n");
            sb.append("  - Value: ").append(correlation.getTelemetry().getValue()).append("\n");
        }
        
        if (correlation.getRecommendedAction() != null) {
            sb.append("\nRecommended Action:\n");
            sb.append("  ").append(correlation.getRecommendedAction()).append("\n");
        }
        
        return sb.toString();
    }
}
