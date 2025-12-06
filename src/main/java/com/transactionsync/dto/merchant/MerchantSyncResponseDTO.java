package com.transactionsync.dto.merchant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantSyncResponseDTO {
    private Long merchantsFetched;
    private Long merchantsCreated;
    private Long merchantsUpdated;
    private String duration;
}

