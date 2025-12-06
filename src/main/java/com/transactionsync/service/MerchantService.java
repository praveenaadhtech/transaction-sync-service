package com.transactionsync.service;

import com.transactionsync.dto.merchant.MerchantDTO;
import com.transactionsync.dto.merchant.MerchantDetailDTO;
import com.transactionsync.dto.merchant.MerchantStatsDTO;
import com.transactionsync.dto.merchant.MerchantUpdateDTO;
import com.transactionsync.dto.response.PageResponseDTO;
import com.transactionsync.exception.ResourceNotFoundException;
import com.transactionsync.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public MerchantService(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    public PageResponseDTO<MerchantDTO> getAllMerchants(Integer page, Integer size, String status, String search) {
        int pageNumber = (page != null && page >= 0) ? page : 0;
        int pageSize = (size != null && size > 0) ? size : 20;

        List<MerchantDTO> merchants = merchantRepository.findAll(pageNumber, pageSize, status, search);
        long totalElements = merchantRepository.countAll(status, search);
        
        int totalPages = (int) Math.ceil((double) totalElements / pageSize);
        int numberOfElements = merchants.size();
        boolean isFirst = pageNumber == 0;
        boolean isLast = pageNumber >= totalPages - 1;

        return new PageResponseDTO<>(
                merchants,
                totalElements,
                totalPages,
                pageSize,
                pageNumber,
                isFirst,
                isLast,
                numberOfElements
        );
    }

    public MerchantDetailDTO getMerchantById(String mid) {
        MerchantDTO merchant = merchantRepository.findByMid(mid);
        if (merchant == null) {
            throw new ResourceNotFoundException("Merchant not found: " + mid);
        }

        MerchantDetailDTO.Statistics statistics = merchantRepository.getMerchantStatistics(mid);

        return MerchantDetailDTO.builder()
                .id(merchant.getId())
                .mid(merchant.getMid())
                .name(merchant.getName())
                .status(merchant.getStatus())
                .createdAt(merchant.getCreatedAt())
                .updatedAt(merchant.getUpdatedAt())
                .lastSyncedAt(merchant.getLastSyncedAt())
                .statistics(statistics)
                .build();
    }

    public MerchantDTO updateMerchant(String mid, MerchantUpdateDTO updateDTO) {
        MerchantDTO existingMerchant = merchantRepository.findByMid(mid);
        if (existingMerchant == null) {
            throw new ResourceNotFoundException("Merchant not found: " + mid);
        }

        String name = updateDTO.getName() != null ? updateDTO.getName() : existingMerchant.getName();
        String status = updateDTO.getStatus() != null ? updateDTO.getStatus() : existingMerchant.getStatus();

        merchantRepository.update(mid, name, status);

        return merchantRepository.findByMid(mid);
    }

    public MerchantStatsDTO getMerchantStats() {
        return merchantRepository.getMerchantStats();
    }
}

