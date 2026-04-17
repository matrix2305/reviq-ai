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
public class ErpSalesAdapter {

    @Qualifier("erpJdbcTemplate")
    private final JdbcTemplate erpJdbcTemplate;

    public List<Map<String, Object>> fetchReceipts(long afterErpId, int limit) {
        return erpJdbcTemplate.queryForList(
                "SELECT TOP (?) ID, KasaZaglavlje, BrojRacuna, Storno, Dokument, " +
                "VremeKreiranja, Vreme, KomitentTip, Komitent, Iznos, " +
                "KarticaLojalnosti, PreciscenIznos, MS, LS1, Predracun, " +
                "InternetPorudzbenica, refundacija " +
                "FROM RacunZaglavlja WHERE ID > ? ORDER BY ID",
                limit, afterErpId
        );
    }

    public List<Map<String, Object>> fetchReceiptItems(long receiptErpId) {
        return erpJdbcTemplate.queryForList(
                "SELECT RacunZaglavlje, RedniBroj, Artikal, Kolicina, Cena, " +
                "IznosMP, Skladiste, PazarTip, CenaCenovnik, Idu_cvetici " +
                "FROM RacunStavke WHERE RacunZaglavlje = ?",
                receiptErpId
        );
    }

    public List<Map<String, Object>> fetchPaymentTypes() {
        return erpJdbcTemplate.queryForList(
                "SELECT Sifra, Naziv FROM VrstePlacanja"
        );
    }

    public List<Map<String, Object>> fetchReceiptPayments(long receiptErpId) {
        return erpJdbcTemplate.queryForList(
                "SELECT RacunZaglavlje, VrstaPlacanja, Skladiste, PazarTip " +
                "FROM RacunPlacanje WHERE RacunZaglavlje = ?",
                receiptErpId
        );
    }

    public List<Map<String, Object>> fetchCashRegisterSessions() {
        return erpJdbcTemplate.queryForList(
                "SELECT ID, OrgJed, Kasa, Datum FROM KasaZaglavlja"
        );
    }
}
