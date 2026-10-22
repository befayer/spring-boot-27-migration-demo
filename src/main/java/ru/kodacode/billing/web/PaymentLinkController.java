package ru.kodacode.billing.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.kodacode.billing.invoice.InvoiceService;

/**
 * Публичная страница оплаты: плательщик открывает ссылку из письма без авторизации.
 */
@RestController
public class PaymentLinkController {

    private final InvoiceService invoices;

    public PaymentLinkController(InvoiceService invoices) {
        this.invoices = invoices;
    }

    @GetMapping("/api/public/pay/{token}")
    public PaymentLinkResponse paymentLink(@PathVariable String token) {
        return invoices.findByPaymentToken(token)
            .map(PaymentLinkResponse::new)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment link not found"));
    }
}
