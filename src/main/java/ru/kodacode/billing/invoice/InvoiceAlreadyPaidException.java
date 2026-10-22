package ru.kodacode.billing.invoice;

public class InvoiceAlreadyPaidException extends RuntimeException {

    public InvoiceAlreadyPaidException(Long id) {
        super("Invoice " + id + " is already paid");
    }
}
