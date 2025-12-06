package com.transactionsync.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantStatsDTO {
    private Long totalMerchants;
    private Long activeMerchants;
    private Long inactiveMerchants;
    private Long merchantsWithTransactions;
    private Instant lastSyncDate;
}

