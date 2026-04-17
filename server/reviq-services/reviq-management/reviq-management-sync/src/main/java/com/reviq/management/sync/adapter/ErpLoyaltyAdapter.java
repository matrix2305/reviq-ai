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
public class ErpLoyaltyAdapter {

    @Qualifier("erpJdbcTemplate")
    private final JdbcTemplate erpJdbcTemplate;

    public List<Map<String, Object>> fetchLoyalCustomers() {
        return erpJdbcTemplate.queryForList(
                "SELECT lk.ID, n.IME, n.PREZIME, n.POL, n.DATUMRODJENJA, " +
                "n.ULICA, n.MESTO, n.PTT, n.TELEFON, n.MOBILNI, n.EMAIL " +
                "FROM LojalniKupci lk LEFT JOIN lojalnikupci_novo n ON lk.ID = n.ID"
        );
    }

    public List<Map<String, Object>> fetchLoyaltyCards() {
        return erpJdbcTemplate.queryForList(
                "SELECT ID, BarKod, Iznos, LojalniKupac, Status FROM KarticaLojalnosti"
        );
    }

    public List<Map<String, Object>> fetchLoyaltyTiers() {
        return erpJdbcTemplate.queryForList(
                "SELECT ID, SIFRA, NAZIV, POPUST, DATUMOD, DATUMDO, " +
                "GRANICNIIZNOS, POENI, PROGRAMLOJALNOSTI " +
                "FROM KATEGORIJAKARTICA"
        );
    }

    public List<Map<String, Object>> fetchCardTierHistory() {
        return erpJdbcTemplate.queryForList(
                "SELECT ID, KARTICALOJALNOSTI, KATEGORIJAKARTICE, DATUMOD, DATUMDO " +
                "FROM KARTICAUKATEGORIJI"
        );
    }
}
