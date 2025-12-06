package com.transactionsync.repository;

import com.transactionsync.dto.merchant.MerchantDTO;
import com.transactionsync.dto.merchant.MerchantDetailDTO;
import com.transactionsync.dto.merchant.MerchantStatsDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class MerchantRepository {

    private final JdbcTemplate jdbcTemplate;

    public MerchantRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final RowMapper<MerchantDTO> MERCHANT_ROW_MAPPER = new RowMapper<MerchantDTO>() {
        @Override
        public MerchantDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return MerchantDTO.builder()
                    .id(rs.getLong("id"))
                    .mid(rs.getString("mid"))
                    .name(rs.getString("name"))
                    .status(rs.getString("status"))
                    .createdAt(rs.getTimestamp("created_at") != null 
                            ? rs.getTimestamp("created_at").toInstant() 
                            : null)
                    .updatedAt(rs.getTimestamp("updated_at") != null 
                            ? rs.getTimestamp("updated_at").toInstant() 
                            : null)
                    .lastSyncedAt(rs.getTimestamp("last_synced_at") != null 
                            ? rs.getTimestamp("last_synced_at").toInstant() 
                            : null)
                    .build();
        }
    };

    public List<MerchantDTO> findAll(int page, int size, String status, String search) {
        StringBuilder sql = new StringBuilder("SELECT id, mid, name, status, created_at, updated_at, last_synced_at FROM merchants WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (mid ILIKE ? OR name ILIKE ?)");
            String searchPattern = "%" + search + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        sql.append(" ORDER BY created_at DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(page * size);

        return jdbcTemplate.query(sql.toString(), MERCHANT_ROW_MAPPER, params.toArray());
    }

    public long countAll(String status, String search) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM merchants WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.trim().isEmpty()) {
            sql.append(" AND status = ?");
            params.add(status);
        }

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (mid ILIKE ? OR name ILIKE ?)");
            String searchPattern = "%" + search + "%";
            params.add(searchPattern);
            params.add(searchPattern);
        }

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }

    public MerchantDTO findByMid(String mid) {
        String sql = "SELECT id, mid, name, status, created_at, updated_at, last_synced_at FROM merchants WHERE mid = ?";
        List<MerchantDTO> results = jdbcTemplate.query(sql, MERCHANT_ROW_MAPPER, mid);
        return results.isEmpty() ? null : results.get(0);
    }

    public MerchantDetailDTO.Statistics getMerchantStatistics(String mid) {
        try {
            String sql = "SELECT " +
                    "COUNT(*) as total_transactions, " +
                    "COALESCE(SUM(amount), 0) as total_amount, " +
                    "MAX(transaction_date) as last_transaction_date " +
                    "FROM transactions WHERE mid = ?";
            
            return jdbcTemplate.queryForObject(sql, new RowMapper<MerchantDetailDTO.Statistics>() {
                @Override
                public MerchantDetailDTO.Statistics mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return MerchantDetailDTO.Statistics.builder()
                            .totalTransactions(rs.getLong("total_transactions"))
                            .totalAmount(rs.getDouble("total_amount"))
                            .lastTransactionDate(rs.getDate("last_transaction_date") != null 
                                    ? rs.getDate("last_transaction_date").toLocalDate() 
                                    : null)
                            .build();
                }
            }, mid);
        } catch (Exception e) {
            return MerchantDetailDTO.Statistics.builder()
                    .totalTransactions(0L)
                    .totalAmount(0.0)
                    .lastTransactionDate(null)
                    .build();
        }
    }

    public void update(String mid, String name, String status) {
        String sql = "UPDATE merchants SET name = ?, status = ?, updated_at = NOW() WHERE mid = ?";
        jdbcTemplate.update(sql, name, status, mid);
    }

    public void saveOrUpdate(String mid, String name, String status) {
        String checkSql = "SELECT COUNT(*) FROM merchants WHERE mid = ?";
        Long count = jdbcTemplate.queryForObject(checkSql, Long.class, mid);
        
        if (count != null && count > 0) {
            String updateSql = "UPDATE merchants SET name = ?, status = ?, updated_at = NOW(), last_synced_at = NOW() WHERE mid = ?";
            jdbcTemplate.update(updateSql, name, status, mid);
        } else {
            String insertSql = "INSERT INTO merchants (mid, name, status, created_at, updated_at, last_synced_at) VALUES (?, ?, ?, NOW(), NOW(), NOW())";
            jdbcTemplate.update(insertSql, mid, name, status);
        }
    }

    public MerchantStatsDTO getMerchantStats() {
        String sql = "SELECT " +
                "COUNT(*) as total_merchants, " +
                "COUNT(CASE WHEN status = 'active' THEN 1 END) as active_merchants, " +
                "COUNT(CASE WHEN status = 'inactive' THEN 1 END) as inactive_merchants, " +
                "MAX(last_synced_at) as last_sync_date " +
                "FROM merchants";
        
        MerchantStatsRowMapper rowMapper = new MerchantStatsRowMapper();
        MerchantStatsDTO stats = jdbcTemplate.queryForObject(sql, rowMapper);
        
        long merchantsWithTransactions = 0;
        try {
            String transactionsSql = "SELECT COUNT(DISTINCT mid) FROM transactions";
            Long count = jdbcTemplate.queryForObject(transactionsSql, Long.class);
            merchantsWithTransactions = count != null ? count : 0L;
        } catch (Exception e) {
            merchantsWithTransactions = 0L;
        }
        
        return MerchantStatsDTO.builder()
                .totalMerchants(stats.getTotalMerchants())
                .activeMerchants(stats.getActiveMerchants())
                .inactiveMerchants(stats.getInactiveMerchants())
                .merchantsWithTransactions(merchantsWithTransactions)
                .lastSyncDate(stats.getLastSyncDate())
                .build();
    }

    private static class MerchantStatsRowMapper implements RowMapper<MerchantStatsDTO> {
        @Override
        public MerchantStatsDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
            return MerchantStatsDTO.builder()
                    .totalMerchants(rs.getLong("total_merchants"))
                    .activeMerchants(rs.getLong("active_merchants"))
                    .inactiveMerchants(rs.getLong("inactive_merchants"))
                    .lastSyncDate(rs.getTimestamp("last_sync_date") != null 
                            ? rs.getTimestamp("last_sync_date").toInstant() 
                            : null)
                    .build();
        }
    }
}

