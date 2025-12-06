package com.transactionsync.controller;

import com.transactionsync.dto.dashboard.HealthResponse;
import com.transactionsync.dto.dashboard.StatsResponse;
import com.transactionsync.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping("/health")
    public ResponseEntity<ApiResponse<HealthResponse>> health() {
        HealthResponse.DatabaseComponent dbComponent = HealthResponse.DatabaseComponent.builder()
                .status("UP")
                .details(Map.of("database", "PostgreSQL"))
                .build();

        HealthResponse.Components components = HealthResponse.Components.builder()
                .db(dbComponent)
                .build();

        HealthResponse healthData = HealthResponse.builder()
                .status("UP")
                .timestamp(Instant.now())
                .message("Transaction Sync Service is running")
                .components(components)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Health status retrieved successfully", healthData));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<StatsResponse>> stats() {
        StatsResponse.TransactionStats transactionStats = StatsResponse.TransactionStats.builder()
                .total(0L)
                .today(0L)
                .thisWeek(0L)
                .thisMonth(0L)
                .build();

        StatsResponse.MerchantStats merchantStats = StatsResponse.MerchantStats.builder()
                .total(0L)
                .active(0L)
                .inactive(0L)
                .build();

        StatsResponse.RetryQueueStats retryQueueStats = StatsResponse.RetryQueueStats.builder()
                .pending(0L)
                .completed(0L)
                .maxRetriesReached(0L)
                .build();

        StatsResponse.SyncStatus syncStatus = StatsResponse.SyncStatus.builder()
                .lastTransactionSync(null)
                .lastMerchantSync(null)
                .lastRetryProcess(null)
                .build();

        StatsResponse.SystemHealth systemHealth = StatsResponse.SystemHealth.builder()
                .status("healthy")
                .database("connected")
                .privvyApi("not_configured")
                .redfynnApi("not_configured")
                .build();

        StatsResponse statsData = StatsResponse.builder()
                .transactions(transactionStats)
                .merchants(merchantStats)
                .retryQueue(retryQueueStats)
                .syncStatus(syncStatus)
                .systemHealth(systemHealth)
                .build();

        return ResponseEntity.ok(ApiResponse.success("Dashboard statistics retrieved successfully", statsData));
    }
}

