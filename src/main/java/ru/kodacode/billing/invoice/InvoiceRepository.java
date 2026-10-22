package ru.kodacode.billing.invoice;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByPaymentToken(String paymentToken);

    List<Invoice> findAllByOrderByIdDesc();
}
