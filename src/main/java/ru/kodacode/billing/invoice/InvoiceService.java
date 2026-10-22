package ru.kodacode.billing.invoice;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class InvoiceService {

    private final InvoiceRepository invoices;
    private final Clock clock;

    public InvoiceService(InvoiceRepository invoices, Clock clock) {
        this.invoices = invoices;
        this.clock = clock;
    }

    @Transactional
    public Invoice issue(String customerId, String currency, List<InvoiceLine> lines) {
        return invoices.save(new Invoice(customerId, currency, lines, Instant.now(clock)));
    }

    @Transactional(readOnly = true)
    public Invoice get(Long id) {
        return invoices.findById(id).orElseThrow(() -> new InvoiceNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Invoice> list() {
        return invoices.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Optional<Invoice> findByPaymentToken(String token) {
        return invoices.findByPaymentToken(token);
    }

    @Transactional
    public Invoice pay(Long id) {
        Invoice invoice = get(id);
        invoice.markPaid(Instant.now(clock));
        return invoice;
    }
}
