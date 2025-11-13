package com.ratip.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ratip.model.CorrelatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatGptClient {
    
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    
    @Value("${ratip.openai.api-key}")
    private String apiKey;
    
    @Value("${ratip.openai.model:gpt-4o-mini}")
    private String model;
    
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    
    public String summarizeEvents(List<CorrelatedEvent> events, String userQuery) {
        try {
            if (apiKey == null || apiKey.equals("your-api-key-here") || apiKey.isEmpty()) {
                log.warn("OpenAI API key not configured, returning mock response");
                return generateMockResponse(events, userQuery);
            }
            
            String context = buildEventContext(events);
            String prompt = buildPrompt(context, userQuery);
            
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "system", "content", 
                        "You are an expert DevOps assistant analyzing telemetry and alarm data. " +
                        "Provide concise, actionable insights."),
                    Map.of("role", "user", "content", prompt)
                ),
                "max_tokens", 500,
                "temperature", 0.7
            );
            
            String requestBodyJson = objectMapper.writeValueAsString(requestBody);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                JsonNode jsonResponse = objectMapper.readTree(response.body());
                String content = jsonResponse.path("choices").get(0)
                        .path("message").path("content").asText();
                
                log.info("ChatGPT response generated for query: {}", userQuery);
                return content;
            } else {
                log.error("ChatGPT API error: {} - {}", response.statusCode(), response.body());
                return generateMockResponse(events, userQuery);
            }
            
        } catch (Exception e) {
            log.error("Error calling ChatGPT API", e);
            return generateMockResponse(events, userQuery);
        }
    }
    
    private String generateMockResponse(List<CorrelatedEvent> events, String userQuery) {
        StringBuilder response = new StringBuilder();
        response.append("AI Analysis (Mock Mode - Set OPENAI_API_KEY for real AI responses)\n\n");
        response.append("Query: ").append(userQuery).append("\n\n");
        response.append("Analysis Summary:\n");
        response.append("Found ").append(events.size()).append(" correlated events in the specified time range.\n\n");
        
        if (!events.isEmpty()) {
            response.append("Top Correlations:\n");
            for (int i = 0; i < Math.min(3, events.size()); i++) {
                CorrelatedEvent event = events.get(i);
                response.append(String.format("%d. %s (Confidence: %.1f%%)\n", 
                        i + 1, event.getDescription(), event.getConfidenceScore() * 100));
                
                if (event.getRecommendedAction() != null) {
                    response.append("   Action: ").append(event.getRecommendedAction()).append("\n");
                }
            }
        }
        
        response.append("\nRecommendation: Monitor these patterns and investigate services with high correlation confidence.");
        
        return response.toString();
    }
    
    private String buildEventContext(List<CorrelatedEvent> events) {
        StringBuilder context = new StringBuilder();
        context.append("Recent Events and Correlations:\n\n");
        
        for (int i = 0; i < Math.min(events.size(), 20); i++) {
            CorrelatedEvent event = events.get(i);
            context.append(String.format("%d. [%s] %s (Confidence: %.1f%%)\n",
                    i + 1,
                    event.getCorrelationTimestamp(),
                    event.getDescription(),
                    event.getConfidenceScore() * 100));
            
            if (event.getAlarm() != null) {
                context.append(String.format("   Alarm: %s - %s (Severity: %s)\n",
                        event.getAlarm().getServiceName(),
                        event.getAlarm().getAlarmName(),
                        event.getAlarm().getSeverity()));
            }
            
            if (event.getTelemetry() != null) {
                context.append(String.format("   Telemetry: %s = %.2f\n",
                        event.getTelemetry().getMetricType(),
                        event.getTelemetry().getValue()));
            }
            context.append("\n");
        }
        
        return context.toString();
    }
    
    private String buildPrompt(String context, String userQuery) {
        return String.format(
                "Based on the following telemetry and alarm data:\n\n%s\n\n" +
                "User Question: %s\n\n" +
                "Provide a concise, actionable answer. Include specific metrics, " +
                "identify root causes, and suggest remediation steps if applicable.",
                context, userQuery);
    }
}
