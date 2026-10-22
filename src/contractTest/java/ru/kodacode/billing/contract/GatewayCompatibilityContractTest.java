package ru.kodacode.billing.contract;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Совместимость с API gateway")
class GatewayCompatibilityContractTest {

    private static final String TRACE_ID = "463ac35c9f6413ad48485a3953bb6124";
    private static final String SPAN_ID = "a2fb4a1d1a96d312";

    private final ContractClient api = new ContractClient();

    @Test
    @DisplayName("Trace id из B3-заголовков gateway возвращается в X-Trace-Id")
    void propagatesB3TraceId() throws Exception {
        HttpResponse<String> response = api.send(api.authorized("/api/invoices")
            .header("X-B3-TraceId", TRACE_ID)
            .header("X-B3-SpanId", SPAN_ID)
            .header("X-B3-Sampled", "1")
            .GET());

        assertEquals(200, response.statusCode(), response.body());
        assertEquals(TRACE_ID, response.headers().firstValue("X-Trace-Id").orElse("<missing>"));
    }

    @Test
    @DisplayName("Запрос с контекстом клиента 16 КБ в заголовке обрабатывается")
    void acceptsLargeClientContextHeader() throws Exception {
        String context = "c".repeat(16 * 1024);

        HttpResponse<String> response = api.send(api.authorized("/api/invoices")
            .header("X-Client-Context", context)
            .GET());

        assertEquals(200, response.statusCode());
    }
}
