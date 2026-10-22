package ru.kodacode.billing.contract;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.kodacode.billing.contract.ContractClient.json;

@DisplayName("Доступ и публичная ссылка на оплату")
class SecurityContractTest {

    private final ContractClient api = new ContractClient();

    @BeforeEach
    void reset() throws Exception {
        ContractClient.resetDatabase();
    }

    @Test
    @DisplayName("Без учётных данных закрытый API отвечает 401")
    void requiresAuthentication() throws Exception {
        HttpResponse<String> response = api.send(api.request("/api/invoices/1").GET());

        assertEquals(401, response.statusCode());
    }

    @Test
    @DisplayName("Health доступен без авторизации")
    void healthIsPublic() throws Exception {
        HttpResponse<String> response = api.send(api.request("/actuator/health").GET());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals("UP", json(response).path("status").asText());
    }

    @Test
    @DisplayName("Плательщик открывает ссылку на оплату без авторизации")
    void paymentLinkIsPublic() throws Exception {
        JsonNode invoice = api.issueInvoice("ACME-42");

        HttpResponse<String> response = api.send(
            api.request("/api/public/pay/" + invoice.path("paymentToken").asText()).GET());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals(invoice.path("number").asText(), json(response).path("number").asText());
        assertEquals("ISSUED", json(response).path("status").asText());
    }

    @Test
    @DisplayName("Устаревшая ссылка на оплату — 404, а не просьба залогиниться")
    void expiredPaymentLinkIsNotFound() throws Exception {
        HttpResponse<String> response = api.send(api.request("/api/public/pay/expired-token").GET());

        assertEquals(404, response.statusCode(), response.body());
        assertEquals(404, json(response).path("status").asInt());
        assertEquals("/api/public/pay/expired-token", json(response).path("path").asText());
    }
}
