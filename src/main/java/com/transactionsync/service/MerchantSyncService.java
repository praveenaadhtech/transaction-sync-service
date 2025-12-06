package com.transactionsync.service;

import com.transactionsync.dto.merchant.MerchantDTO;
import com.transactionsync.dto.merchant.MerchantSyncResponseDTO;
import com.transactionsync.dto.integration.PrivvyMerchantDTO;
import com.transactionsync.integration.PrivvyApiClient;
import com.transactionsync.repository.MerchantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MerchantSyncService {

    private final PrivvyApiClient privvyApiClient;
    private final MerchantRepository merchantRepository;

    public MerchantSyncService(PrivvyApiClient privvyApiClient, MerchantRepository merchantRepository) {
        this.privvyApiClient = privvyApiClient;
        this.merchantRepository = merchantRepository;
    }

    @Transactional
    public MerchantSyncResponseDTO syncMerchants() {
        Instant startTime = Instant.now();

        List<PrivvyMerchantDTO> privvyMerchants = privvyApiClient.fetchMerchants();
        long merchantsFetched = privvyMerchants.size();
        long merchantsCreated = 0;
        long merchantsUpdated = 0;

        for (PrivvyMerchantDTO privvyMerchant : privvyMerchants) {
            String mid = privvyMerchant.getMID();
            if (mid == null || mid.trim().isEmpty()) {
                continue;
            }

            String name = privvyMerchant.getMerchName() != null 
                    ? privvyMerchant.getMerchName() 
                    : (privvyMerchant.getLegalName() != null ? privvyMerchant.getLegalName() : "Unknown Merchant");
            
            String status = "active";
            if (privvyMerchant.getCustomer_status() != null) {
                String customerStatus = privvyMerchant.getCustomer_status().toLowerCase();
                if (customerStatus.contains("inactive") || customerStatus.contains("suspended")) {
                    status = "inactive";
                }
            }

            MerchantDTO existingMerchant = merchantRepository.findByMid(mid);
            merchantRepository.saveOrUpdate(mid, name, status);
            
            if (existingMerchant == null) {
                merchantsCreated++;
            } else {
                merchantsUpdated++;
            }
        }

        Instant endTime = Instant.now();
        long durationMillis = java.time.Duration.between(startTime, endTime).toMillis();
        String duration = String.format("%.1fs", durationMillis / 1000.0);

        return MerchantSyncResponseDTO.builder()
                .merchantsFetched(merchantsFetched)
                .merchantsCreated(merchantsCreated)
                .merchantsUpdated(merchantsUpdated)
                .duration(duration)
                .build();
    }
}

