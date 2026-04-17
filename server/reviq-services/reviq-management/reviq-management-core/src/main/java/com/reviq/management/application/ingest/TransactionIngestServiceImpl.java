package com.reviq.management.application.ingest;

import com.reviq.management.api.ingest.TransactionIngestService;
import com.reviq.management.api.ingest.dto.TransactionLineRequest;
import com.reviq.management.api.ingest.dto.TransactionPaymentRequest;
import com.reviq.management.api.ingest.dto.TransactionRequest;
import com.reviq.management.domain.account.repository.AccountRepository;
import com.reviq.management.domain.loyalty.repository.LoyaltyCardRepository;
import com.reviq.management.domain.partner.repository.PartnerRepository;
import com.reviq.management.domain.product.repository.ProductRepository;
import com.reviq.management.domain.transaction.entity.Transaction;
import com.reviq.management.domain.transaction.entity.TransactionLine;
import com.reviq.management.domain.transaction.entity.TransactionPayment;
import com.reviq.management.domain.transaction.repository.PaymentMethodRepository;
import com.reviq.management.domain.transaction.repository.RegisterSessionRepository;
import com.reviq.management.domain.transaction.repository.TransactionRepository;
import com.reviq.shared.enums.TransactionLineType;
import com.reviq.shared.enums.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionIngestServiceImpl implements TransactionIngestService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final AccountRepository accountRepository;
    private final RegisterSessionRepository registerSessionRepository;
    private final LoyaltyCardRepository loyaltyCardRepository;
    private final PartnerRepository partnerRepository;

    @Override
    @Transactional
    public UUID upsert(TransactionRequest request) {
        Transaction transaction = transactionRepository.findByExternalId(request.getExternalId())
                .orElseGet(Transaction::new);

        transaction.setExternalId(request.getExternalId());
        transaction.setTransactionNumber(request.getTransactionNumber());
        transaction.setType(TransactionType.valueOf(request.getType()));
        transaction.setChannel(request.getChannel());
        transaction.setDocumentType(request.getDocumentType());
        transaction.setDiscount(request.getDiscount());
        transaction.setAmount(request.getAmount());
        transaction.setNetAmount(request.getNetAmount());
        transaction.setRefund(request.getRefund());
        transaction.setTransactionTime(request.getTransactionTime());
        transaction.setCreatedTime(request.getCreatedTime());
        transaction.setLoyaltyPointsMaster(request.getLoyaltyPointsMaster());
        transaction.setLoyaltyPointsPromo(request.getLoyaltyPointsPromo());
        transaction.setOnlineOrderId(request.getOnlineOrderId());
        transaction.setSyncedAt(LocalDateTime.now());

        if (request.getIsPreorder() != null) {
            transaction.setIsPreorder(request.getIsPreorder());
        }
        if (request.getAccountExternalId() != null) {
            accountRepository.findByExternalId(request.getAccountExternalId())
                    .ifPresent(transaction::setAccount);
        }
        if (request.getRegisterSessionExternalId() != null) {
            registerSessionRepository.findByExternalId(request.getRegisterSessionExternalId())
                    .ifPresent(transaction::setRegisterSession);
        }
        if (request.getLoyaltyCardExternalId() != null) {
            loyaltyCardRepository.findByExternalId(request.getLoyaltyCardExternalId())
                    .ifPresent(transaction::setLoyaltyCard);
        }
        if (request.getPartnerExternalId() != null) {
            partnerRepository.findByExternalId(request.getPartnerExternalId())
                    .ifPresent(transaction::setPartner);
        }

        // Replace lines
        transaction.getLines().clear();
        if (request.getLines() != null) {
            for (TransactionLineRequest lineReq : request.getLines()) {
                TransactionLine line = TransactionLine.builder()
                        .transaction(transaction)
                        .lineNumber(lineReq.getLineNumber())
                        .quantity(lineReq.getQuantity())
                        .price(lineReq.getPrice())
                        .listPrice(lineReq.getListPrice())
                        .discount(lineReq.getDiscount())
                        .purchasePrice(lineReq.getPurchasePrice())
                        .retailAmount(lineReq.getRetailAmount())
                        .taxAmount(lineReq.getTaxAmount())
                        .warehouse(lineReq.getWarehouse())
                        .salesType(lineReq.getSalesType())
                        .earnLoyaltyPoints(lineReq.getEarnLoyaltyPoints())
                        .refund(lineReq.getRefund())
                        .build();

                if (lineReq.getLineType() != null) {
                    line.setLineType(TransactionLineType.valueOf(lineReq.getLineType()));
                }
                if (lineReq.getProductExternalId() != null) {
                    productRepository.findByExternalId(lineReq.getProductExternalId())
                            .ifPresent(line::setProduct);
                }

                transaction.getLines().add(line);
            }
        }

        // Replace payments
        transaction.getPayments().clear();
        if (request.getPayments() != null) {
            for (TransactionPaymentRequest payReq : request.getPayments()) {
                TransactionPayment payment = TransactionPayment.builder()
                        .transaction(transaction)
                        .amount(payReq.getAmount())
                        .warehouse(payReq.getWarehouse())
                        .salesType(payReq.getSalesType())
                        .build();

                if (payReq.getPaymentMethodCode() != null) {
                    paymentMethodRepository.findByCode(payReq.getPaymentMethodCode())
                            .ifPresent(payment::setPaymentMethod);
                }

                transaction.getPayments().add(payment);
            }
        }

        return transactionRepository.save(transaction).getId();
    }

    @Override
    @Transactional
    public List<UUID> upsertBatch(List<TransactionRequest> requests) {
        return requests.stream().map(this::upsert).toList();
    }
}
