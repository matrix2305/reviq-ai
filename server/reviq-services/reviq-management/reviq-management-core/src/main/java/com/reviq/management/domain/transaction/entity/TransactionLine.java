package com.reviq.management.domain.transaction.entity;

import com.reviq.management.domain.product.entity.Product;
import com.reviq.shared.entity.BaseEntity;
import com.reviq.shared.enums.TransactionLineType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "transaction_line")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionLine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "quantity", precision = 16, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", precision = 16, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "list_price", precision = 16, scale = 2)
    private BigDecimal listPrice;

    @Column(name = "discount", precision = 16, scale = 2)
    private BigDecimal discount;

    @Column(name = "purchase_price", precision = 16, scale = 2)
    private BigDecimal purchasePrice;

    @Column(name = "retail_amount", precision = 16, scale = 2, nullable = false)
    private BigDecimal retailAmount;

    @Column(name = "tax_amount", precision = 16, scale = 2)
    private BigDecimal taxAmount;

    @Column(name = "warehouse")
    private String warehouse;

    @Column(name = "sales_type")
    private String salesType;

    @Column(name = "earn_loyalty_points")
    private Boolean earnLoyaltyPoints;

    @Enumerated(EnumType.STRING)
    @Column(name = "line_type", nullable = false)
    private TransactionLineType lineType;

    @Column(name = "refund", precision = 16, scale = 2)
    private BigDecimal refund;
}
