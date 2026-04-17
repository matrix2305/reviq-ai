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
public class ErpStoreAdapter {

    @Qualifier("erpJdbcTemplate")
    private final JdbcTemplate erpJdbcTemplate;

    public List<Map<String, Object>> fetchAllStores() {
        return erpJdbcTemplate.queryForList(
                "SELECT o.Sifra, o.Otac, o.Naziv, o.Ulica, o.Mjesto, " +
                "s.Loyalty_store_id " +
                "FROM OrgJed o LEFT JOIN Org_Jed_Store s ON o.Sifra = s.Org_jed_fk AND s.Active = 1"
        );
    }

    public List<Map<String, Object>> fetchZones() {
        return erpJdbcTemplate.queryForList(
                "SELECT [SIFRA POSLOVNICE], [NAZIV POSLOVNICE], [REGION/ZONA] FROM ZONE"
        );
    }
}
