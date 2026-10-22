package ru.kodacode.billing.contract;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.kodacode.billing.contract.ContractClient.json;

@DisplayName("Счета: основной сценарий")
class InvoiceApiContractTest {

    private final ContractClient api = new ContractClient();

    @BeforeEach
    void reset() throws Exception {
        ContractClient.resetDatabase();
    }

    @Test
    @DisplayName("POST /api/invoices выставляет счёт и возвращает Location")
    void issuesInvoice() throws Exception {
        HttpResponse<String> response = api.post("/api/invoices", "{"
            + "\"customerId\":\"ACME-42\",\"currency\":\"EUR\","
            + "\"lines\":[{\"description\":\"Hosting\",\"quantity\":3,\"unitPrice\":19.99},"
            + "{\"description\":\"Support\",\"quantity\":1,\"unitPrice\":150}]}");

        assertEquals(201, response.statusCode(), response.body());
        JsonNode invoice = json(response);
        long id = invoice.path("id").asLong();
        assertTrue(id > 0);
        assertEquals(String.format("INV-%06d", id), invoice.path("number").asText());
        assertEquals("ACME-42", invoice.path("customerId").asText());
        assertEquals("ISSUED", invoice.path("status").asText());
        assertEquals(0, new BigDecimal("209.97").compareTo(invoice.path("totalAmount").decimalValue()));
        assertEquals(2, invoice.path("lines").size());
        assertTrue(response.headers().firstValue("Location").orElse("").endsWith("/api/invoices/" + id));
    }

    @Test
    @DisplayName("GET /api/invoices/{id} возвращает выставленный счёт")
    void readsInvoice() throws Exception {
        long id = api.issueInvoice("ACME-42").path("id").asLong();

        HttpResponse<String> response = api.get("/api/invoices/" + id);

        assertEquals(200, response.statusCode(), response.body());
        assertEquals("ACME-42", json(response).path("customerId").asText());
    }

    @Test
    @DisplayName("Оплата переводит счёт в PAID, повторная оплата — 409")
    void paysInvoiceOnlyOnce() throws Exception {
        long id = api.issueInvoice("ACME-42").path("id").asLong();

        HttpResponse<String> paid = api.post("/api/invoices/" + id + "/payments", "");
        assertEquals(200, paid.statusCode(), paid.body());
        assertEquals("PAID", json(paid).path("status").asText());
        assertTrue(json(paid).hasNonNull("paidAt"));

        HttpResponse<String> again = api.post("/api/invoices/" + id + "/payments", "");
        assertEquals(409, again.statusCode(), again.body());
        assertEquals("INVOICE_ALREADY_PAID", json(again).path("code").asText());
    }

    @Test
    @DisplayName("Неизвестный счёт — 404 с кодом INVOICE_NOT_FOUND")
    void unknownInvoice() throws Exception {
        HttpResponse<String> response = api.get("/api/invoices/999999");

        assertEquals(404, response.statusCode(), response.body());
        assertEquals("INVOICE_NOT_FOUND", json(response).path("code").asText());
    }

    @Test
    @DisplayName("Невалидный счёт — 400 с кодом VALIDATION_FAILED")
    void rejectsInvalidInvoice() throws Exception {
        HttpResponse<String> response = api.post("/api/invoices",
            "{\"customerId\":\"\",\"currency\":\"euro\",\"lines\":[]}");

        assertEquals(400, response.statusCode(), response.body());
        assertEquals("VALIDATION_FAILED", json(response).path("code").asText());
    }

    @Test
    @DisplayName("Мобильный клиент 3.x запрашивает список со слэшем на конце: GET /api/invoices/")
    void listsInvoicesWithTrailingSlash() throws Exception {
        api.issueInvoice("ACME-1");
        api.issueInvoice("ACME-2");

        HttpResponse<String> response = api.get("/api/invoices/");

        assertEquals(200, response.statusCode(), response.body());
        JsonNode list = json(response);
        assertEquals(2, list.size());
        assertEquals("ACME-2", list.get(0).path("customerId").asText());
    }
}
