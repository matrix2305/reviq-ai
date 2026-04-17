package com.reviq.tenancy.domain.entity;

import com.reviq.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tenant_database")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantDatabase extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false, unique = true)
    private Tenant tenant;

    @Column(name = "db_host", nullable = false)
    private String dbHost;

    @Column(name = "db_port", nullable = false)
    @Builder.Default
    private Integer dbPort = 5432;

    @Column(name = "db_name", nullable = false)
    private String dbName;

    @Column(name = "db_username", nullable = false)
    private String dbUsername;

    @Column(name = "db_password", nullable = false)
    private String dbPassword;

    @Column(name = "management_schema_provisioned")
    @Builder.Default
    private Boolean managementSchemaProvisioned = false;

    @Column(name = "ai_schema_provisioned")
    @Builder.Default
    private Boolean aiSchemaProvisioned = false;

    @Transient
    public String getJdbcUrl() {
        return "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;
    }
}
