package ru.kodacode.billing.invoice;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InvoiceTest {

    @Test
    void sumsLinesWithTwoDecimalPlaces() {
        Invoice invoice = new Invoice("ACME", "EUR", List.of(
            new InvoiceLine("Hosting", 3, new BigDecimal("19.99")),
            new InvoiceLine("Support", 1, new BigDecimal("150"))
        ), Instant.EPOCH);

        assertThat(invoice.getTotalAmount()).isEqualByComparingTo("209.97");
        assertThat(invoice.getTotalAmount().scale()).isEqualTo(2);
        assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.ISSUED);
    }

    @Test
    void cannotBePaidTwice() {
        Invoice invoice = new Invoice("ACME", "EUR", List.of(
            new InvoiceLine("Hosting", 1, BigDecimal.TEN)
        ), Instant.EPOCH);

        invoice.markPaid(Instant.EPOCH);

        assertThatThrownBy(() -> invoice.markPaid(Instant.EPOCH))
            .isInstanceOf(InvoiceAlreadyPaidException.class);
    }
}
