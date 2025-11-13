package com.ratip.service;

import com.ratip.model.TelemetryEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
@Component
public class SlidingWindowAggregator {
    
    private final ConcurrentLinkedQueue<TelemetryEvent> eventWindow = new ConcurrentLinkedQueue<>();
    private static final Duration WINDOW_SIZE = Duration.ofMinutes(15);
    
    public void addEvent(TelemetryEvent event) {
        eventWindow.offer(event);
        cleanOldEvents();
    }
    
    public List<TelemetryEvent> getEventsInWindow() {
        cleanOldEvents();
        return new ArrayList<>(eventWindow);
    }
    
    public List<TelemetryEvent> getEventsByService(String serviceName) {
        cleanOldEvents();
        return eventWindow.stream()
                .filter(event -> event.getServiceName().equals(serviceName))
                .toList();
    }
    
    private void cleanOldEvents() {
        Instant cutoffTime = Instant.now().minus(WINDOW_SIZE);
        eventWindow.removeIf(event -> event.getTimestamp().isBefore(cutoffTime));
    }
}
