package com.reviq.management.domain.transaction.entity;

import com.reviq.management.domain.account.entity.Account;
import com.reviq.management.domain.loyalty.entity.LoyaltyCard;
import com.reviq.management.domain.partner.entity.Partner;
import com.reviq.shared.entity.SyncableEntity;
import com.reviq.shared.enums.TransactionType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "transaction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction extends SyncableEntity {

    @Column(name = "transaction_number", nullable = false)
    private Long transactionNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "channel")
    private String channel;

    @Column(name = "document_type")
    private Integer documentType;

    @Column(name = "discount", precision = 16, scale = 2)
    private BigDecimal discount;

    @Column(name = "amount", precision = 16, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "net_amount", precision = 16, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "refund", precision = 16, scale = 2)
    private BigDecimal refund;

    @Column(name = "transaction_time", nullable = false)
    private LocalDateTime transactionTime;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "register_session_id")
    private RegisterSession registerSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loyalty_card_id")
    private LoyaltyCard loyaltyCard;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    private Partner partner;

    @Column(name = "loyalty_points_master")
    private Integer loyaltyPointsMaster;

    @Column(name = "loyalty_points_promo")
    private Integer loyaltyPointsPromo;

    @Column(name = "is_preorder")
    @Builder.Default
    private Boolean isPreorder = false;

    @Column(name = "online_order_id")
    private String onlineOrderId;

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransactionLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TransactionPayment> payments = new ArrayList<>();
}
