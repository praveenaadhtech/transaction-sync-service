package com.transactionsync.controller;

import com.transactionsync.dto.merchant.MerchantDTO;
import com.transactionsync.dto.merchant.MerchantDetailDTO;
import com.transactionsync.dto.merchant.MerchantStatsDTO;
import com.transactionsync.dto.merchant.MerchantSyncResponseDTO;
import com.transactionsync.dto.merchant.MerchantUpdateDTO;
import com.transactionsync.dto.response.ApiResponse;
import com.transactionsync.dto.response.PageResponseDTO;
import com.transactionsync.service.MerchantService;
import com.transactionsync.service.MerchantSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/merchants")
public class MerchantController {

    private final MerchantService merchantService;
    private final MerchantSyncService merchantSyncService;

    public MerchantController(MerchantService merchantService, MerchantSyncService merchantSyncService) {
        this.merchantService = merchantService;
        this.merchantSyncService = merchantSyncService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponseDTO<MerchantDTO>>> getAllMerchants(
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search) {
        
        PageResponseDTO<MerchantDTO> pageResponse = merchantService.getAllMerchants(page, size, status, search);
        return ResponseEntity.ok(ApiResponse.success("Merchants retrieved successfully", pageResponse));
    }

    @GetMapping("/{mid}")
    public ResponseEntity<ApiResponse<MerchantDetailDTO>> getMerchantById(@PathVariable String mid) {
        MerchantDetailDTO merchant = merchantService.getMerchantById(mid);
        return ResponseEntity.ok(ApiResponse.success("Merchant retrieved successfully", merchant));
    }

    @PutMapping("/{mid}")
    public ResponseEntity<ApiResponse<MerchantDTO>> updateMerchant(
            @PathVariable String mid,
            @RequestBody MerchantUpdateDTO updateDTO) {
        MerchantDTO updatedMerchant = merchantService.updateMerchant(mid, updateDTO);
        return ResponseEntity.ok(ApiResponse.success("Merchant updated successfully", updatedMerchant));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<MerchantStatsDTO>> getMerchantStats() {
        MerchantStatsDTO stats = merchantService.getMerchantStats();
        return ResponseEntity.ok(ApiResponse.success("Merchant statistics retrieved successfully", stats));
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<MerchantSyncResponseDTO>> syncMerchants() {
        MerchantSyncResponseDTO syncResponse = merchantSyncService.syncMerchants();
        return ResponseEntity.ok(ApiResponse.success("Merchant sync completed", syncResponse));
    }
}

