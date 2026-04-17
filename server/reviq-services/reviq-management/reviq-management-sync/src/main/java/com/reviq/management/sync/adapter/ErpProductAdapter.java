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
public class ErpProductAdapter {

    @Qualifier("erpJdbcTemplate")
    private final JdbcTemplate erpJdbcTemplate;

    public List<Map<String, Object>> fetchAllProducts() {
        return erpJdbcTemplate.queryForList(
                "SELECT a.ID, a.SIFRA, a.NAZIV, a.ROBNAGRUPA, a.TIP, a.PROIZVODJAC, " +
                "ao.OSNOVNASIFRA, ao.BREND " +
                "FROM Artikli a LEFT JOIN ArtikliOsobine ao ON a.ID = ao.ARTIKAL"
        );
    }

    public List<Map<String, Object>> fetchAllBrands() {
        return erpJdbcTemplate.queryForList(
                "SELECT SIFRA, NAZIV, JEDINICAMERE, PL FROM Brend"
        );
    }

    public List<Map<String, Object>> fetchAllProductGroups() {
        return erpJdbcTemplate.queryForList(
                "SELECT SIFRA, NAZIV, Nadredjena FROM RobneGrupe"
        );
    }

    public List<Map<String, Object>> fetchAllPromotions() {
        return erpJdbcTemplate.queryForList(
                "SELECT z.ID, z.VremeOd, z.VremeDo, s.Artikal, s.Cena, s.Popust " +
                "FROM ArtikliSnizenjaZag z JOIN ArtikliSnizenjaStv s ON z.ID = s.ArtikliSnizenjeZag"
        );
    }
}
