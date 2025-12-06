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
public class MerchantDTO {
    private Long id;
    private String mid;
    private String name;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastSyncedAt;
}

