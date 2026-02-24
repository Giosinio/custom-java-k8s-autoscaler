package com.csubb.dissertation.customautoscaler.prometheus;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Slf4j
@Component
public class PrometheusClient {

    @Value("${prometheus.base-url}")
    private String PROMETHEUS_BASE_URL;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Autowired
    public PrometheusClient(ObjectMapper objectMapper) {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = objectMapper;
    }

    public Double getPrometheusMetrics(String namespace, String deploymentName, PrometheusQueryType prometheusQueryType) {
        String promqlQuery = URLEncoder.encode(PrometheusQueryFactory.createQuery(prometheusQueryType, namespace, deploymentName), StandardCharsets.UTF_8);

        URI uri = URI.create(PROMETHEUS_BASE_URL + "/api/v1/query?query=" + promqlQuery);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return extractValueFromHttpResponse(response);
        } catch (IOException | InterruptedException exception) {
            log.error("Exception: ", exception);
        }

        return null;
    }

    private Double extractValueFromHttpResponse(HttpResponse<String> response) throws IOException {
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Prometheus returned status " + response.statusCode()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        JsonNode result = root.path("data").path("result");
        if (!result.isArray() || result.isEmpty()) {
            return null;
        }

        JsonNode valueNode = result.get(0).path("value");
        return valueNode.size() >= 2
                ? valueNode.get(1).asDouble()
                : null;
    }
}
