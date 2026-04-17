package com.reviq.management.sync.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ErpInventoryAdapter {

    @Qualifier("erpJdbcTemplate")
    private final JdbcTemplate erpJdbcTemplate;

    public List<Map<String, Object>> fetchCurrentStock() {
        return erpJdbcTemplate.queryForList(
                "SELECT Skladiste, Artikal, Kolicina FROM Zaliha"
        );
    }

    public List<Map<String, Object>> fetchStockHistory() {
        return erpJdbcTemplate.queryForList(
                "SELECT id, Skladiste, Artikal, Kolicina, datum FROM ZalihaHistory"
        );
    }
}
