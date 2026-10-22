package ru.kodacode.billing.web;

import ru.kodacode.billing.invoice.Invoice;

import java.math.BigDecimal;

public class PaymentLinkResponse {

    private final String number;
    private final BigDecimal amountDue;
    private final String currency;
    private final String status;

    public PaymentLinkResponse(Invoice invoice) {
        this.number = invoice.getNumber();
        this.amountDue = invoice.getTotalAmount();
        this.currency = invoice.getCurrency();
        this.status = invoice.getStatus().name();
    }

    public String getNumber() {
        return number;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public String getCurrency() {
        return currency;
    }

    public String getStatus() {
        return status;
    }
}
