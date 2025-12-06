package com.transactionsync.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthResponse {
    private String status;
    private Instant timestamp;
    private String message;
    private Components components;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Components {
        private DatabaseComponent db;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DatabaseComponent {
        private String status;
        private Map<String, String> details;
    }
}

