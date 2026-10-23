package ru.kodacode.billing.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.kodacode.billing.invoice.Invoice;
import ru.kodacode.billing.invoice.InvoiceLine;
import ru.kodacode.billing.invoice.InvoiceService;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

    private final InvoiceService invoices;

    public InvoiceController(InvoiceService invoices) {
        this.invoices = invoices;
    }

    @PostMapping({"", "/"})
    public ResponseEntity<InvoiceResponse> issue(@Valid @RequestBody CreateInvoiceRequest request) {
        List<InvoiceLine> lines = request.getLines().stream()
            .map(line -> new InvoiceLine(line.getDescription(), line.getQuantity(), line.getUnitPrice()))
            .collect(Collectors.toList());
        Invoice invoice = invoices.issue(request.getCustomerId(), request.getCurrency(), lines);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/api/invoices/{id}")
            .buildAndExpand(invoice.getId())
            .toUri();
        return ResponseEntity.created(location).body(InvoiceResponse.from(invoice));
    }

    // В Spring 6 слэш на конце больше не матчится автоматически, а мобильный клиент 3.x
    // запрашивает именно /api/invoices/. Явные пути сохраняют поведение Boot 2.7.
    @GetMapping({"", "/"})
    public List<InvoiceResponse> list() {
        return invoices.list().stream().map(InvoiceResponse::from).collect(Collectors.toList());
    }

    @GetMapping({"/{id}", "/{id}/"})
    public InvoiceResponse get(@PathVariable Long id) {
        return InvoiceResponse.from(invoices.get(id));
    }

    @PostMapping({"/{id}/payments", "/{id}/payments/"})
    public InvoiceResponse pay(@PathVariable Long id) {
        return InvoiceResponse.from(invoices.pay(id));
    }
}
