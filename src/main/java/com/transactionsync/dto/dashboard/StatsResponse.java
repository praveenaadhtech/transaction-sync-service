package com.transactionsync.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
    private TransactionStats transactions;
    private MerchantStats merchants;
    private RetryQueueStats retryQueue;
    private SyncStatus syncStatus;
    private SystemHealth systemHealth;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionStats {
        private Long total;
        private Long today;
        private Long thisWeek;
        private Long thisMonth;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantStats {
        private Long total;
        private Long active;
        private Long inactive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetryQueueStats {
        private Long pending;
        private Long completed;
        private Long maxRetriesReached;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SyncStatus {
        private Instant lastTransactionSync;
        private Instant lastMerchantSync;
        private Instant lastRetryProcess;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private String status;
        private String database;
        private String privvyApi;
        private String redfynnApi;
    }
}

