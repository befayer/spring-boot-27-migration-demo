package ru.kodacode.billing.web;

import ru.kodacode.billing.invoice.Invoice;
import ru.kodacode.billing.invoice.InvoiceLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class InvoiceResponse {

    private final Long id;
    private final String number;
    private final String customerId;
    private final String currency;
    private final String status;
    private final BigDecimal totalAmount;
    private final String paymentToken;
    private final Instant issuedAt;
    private final Instant paidAt;
    private final List<LineResponse> lines;

    private InvoiceResponse(Invoice invoice) {
        this.id = invoice.getId();
        this.number = invoice.getNumber();
        this.customerId = invoice.getCustomerId();
        this.currency = invoice.getCurrency();
        this.status = invoice.getStatus().name();
        this.totalAmount = invoice.getTotalAmount();
        this.paymentToken = invoice.getPaymentToken();
        this.issuedAt = invoice.getIssuedAt();
        this.paidAt = invoice.getPaidAt();
        this.lines = invoice.getLines().stream().map(LineResponse::new).collect(Collectors.toList());
    }

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice);
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
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

    public List<LineResponse> getLines() {
        return lines;
    }

    public static class LineResponse {

        private final String description;
        private final int quantity;
        private final BigDecimal unitPrice;
        private final BigDecimal total;

        LineResponse(InvoiceLine line) {
            this.description = line.getDescription();
            this.quantity = line.getQuantity();
            this.unitPrice = line.getUnitPrice();
            this.total = line.total();
        }

        public String getDescription() {
            return description;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }

        public BigDecimal getTotal() {
            return total;
        }
    }
}
