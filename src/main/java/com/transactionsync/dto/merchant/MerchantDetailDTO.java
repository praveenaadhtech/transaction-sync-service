package com.transactionsync.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantDetailDTO {
    private Long id;
    private String mid;
    private String name;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSyncedAt;
    private Statistics statistics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalTransactions;
        private Double totalAmount;
        private LocalDate lastTransactionDate;
    }
}

