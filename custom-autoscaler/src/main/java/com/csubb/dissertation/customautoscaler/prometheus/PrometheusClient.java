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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    public List<String> getServicesFromNamespace(String namespace) {
        HttpResponse<String> response = executePrometheusQuery(PrometheusQueryType.NAMESPACE_SERVICES, namespace);
        if(Objects.isNull(response)) {
            throw new RuntimeException("Failed to fetch services from namespace " + namespace);
        }
        return extractStringListFromHttpResponse(response);
    }

    public Double getPrometheusMetrics(String namespace, String deploymentName, PrometheusQueryType prometheusQueryType) {
        HttpResponse<String> response = executePrometheusQuery(prometheusQueryType, namespace, deploymentName);
        if(Objects.isNull(response)) {
            log.error("Failed to fetch metric {} for application {}, returning null", prometheusQueryType, deploymentName);
            return null;
        }
        Double value = extractDoubleFromHttpResponse(response);
        if (Objects.isNull(value)) {
            log.warn("Prometheus returned no value for metric {} for application {}, returning null", prometheusQueryType, deploymentName);
            return null;
        }
        return prometheusQueryType.getValueConverter().apply(value);
    }

    private HttpResponse<String> executePrometheusQuery(PrometheusQueryType prometheusQueryType, String ... parameters) {
        String promqlQuery = URLEncoder.encode(PrometheusQueryFactory.createQuery(prometheusQueryType, parameters), StandardCharsets.UTF_8);

        URI uri = URI.create(PROMETHEUS_BASE_URL + "/api/v1/query?query=" + promqlQuery);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException exception) {
            log.error("Exception: ", exception);
        }

        return null;
    }

    private Double extractDoubleFromHttpResponse(HttpResponse<String> response) {
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

    List<String> extractStringListFromHttpResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Prometheus returned status " + response.statusCode()
            );
        }

        JsonNode root = objectMapper.readTree(response.body());

        JsonNode result = root.path("data").path("result");
        if (!result.isArray() || result.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> services = new ArrayList<>();
        for(JsonNode item : result) {
            String serviceName = item.path("metric").path("service").asString();
            services.add(serviceName);
        }

        return services;
    }
}
