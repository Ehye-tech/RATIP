package com.ratip.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CorrelatedEvent {
    private String id;
    private String correlationType;
    private Double confidenceScore;
    private String description;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant correlationTimestamp;
    
    private AlarmEvent alarm;
    private TelemetryEvent telemetry;
    private String rootCause;
    private String recommendedAction;
}
