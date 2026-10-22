package ru.kodacode.billing.invoice;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "payment_token", nullable = false, unique = true)
    private String paymentToken;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "invoice_lines", joinColumns = @JoinColumn(name = "invoice_id"))
    @OrderColumn(name = "line_no")
    private List<InvoiceLine> lines = new ArrayList<>();

    protected Invoice() {
    }

    public Invoice(String customerId, String currency, List<InvoiceLine> lines, Instant issuedAt) {
        this.customerId = customerId;
        this.currency = currency;
        this.lines = new ArrayList<>(lines);
        this.status = InvoiceStatus.ISSUED;
        this.totalAmount = lines.stream()
            .map(InvoiceLine::total)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .setScale(2, RoundingMode.HALF_UP);
        this.paymentToken = UUID.randomUUID().toString().replace("-", "");
        this.issuedAt = issuedAt;
    }

    public void markPaid(Instant paidAt) {
        if (status == InvoiceStatus.PAID) {
            throw new InvoiceAlreadyPaidException(id);
        }
        this.status = InvoiceStatus.PAID;
        this.paidAt = paidAt;
    }

    public String getNumber() {
        return String.format("INV-%06d", id);
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public List<InvoiceLine> getLines() {
        return lines;
    }
}
