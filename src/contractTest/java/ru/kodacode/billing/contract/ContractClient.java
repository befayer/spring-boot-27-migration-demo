package ru.kodacode.billing.contract;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.Duration;
import java.util.Base64;

/**
 * Клиент, который ведёт себя как внешние потребители API.
 * Ничего не знает о Spring и о внутреннем устройстве сервиса.
 */
final class ContractClient {

    static final ObjectMapper JSON = new ObjectMapper();

    private static final String BASIC_AUTH = "Basic " + Base64.getEncoder()
        .encodeToString("billing:billing-secret".getBytes(StandardCharsets.UTF_8));

    private final String baseUrl = System.getProperty("targetBaseUrl");
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build();

    HttpRequest.Builder request(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/json");
    }

    HttpRequest.Builder authorized(String path) {
        return request(path).header("Authorization", BASIC_AUTH);
    }

    HttpResponse<String> send(HttpRequest.Builder builder) throws Exception {
        return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    HttpResponse<String> get(String path) throws Exception {
        return send(authorized(path).GET());
    }

    HttpResponse<String> post(String path, String json) throws Exception {
        return send(authorized(path)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(json)));
    }

    JsonNode issueInvoice(String customerId) throws Exception {
        HttpResponse<String> response = post("/api/invoices", "{"
            + "\"customerId\":\"" + customerId + "\","
            + "\"currency\":\"EUR\","
            + "\"lines\":["
            + "{\"description\":\"Hosting, October\",\"quantity\":3,\"unitPrice\":19.99},"
            + "{\"description\":\"Priority support\",\"quantity\":1,\"unitPrice\":150}"
            + "]}");
        if (response.statusCode() != 201) {
            throw new AssertionError("Expected 201 when issuing invoice, got "
                + response.statusCode() + ": " + response.body());
        }
        return JSON.readTree(response.body());
    }

    static JsonNode json(HttpResponse<String> response) throws Exception {
        return JSON.readTree(response.body());
    }

    static void resetDatabase() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                System.getProperty("dbUrl"), System.getProperty("dbUser"), System.getProperty("dbPassword"));
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM invoice_lines");
            statement.executeUpdate("DELETE FROM invoices");
        }
    }
}
