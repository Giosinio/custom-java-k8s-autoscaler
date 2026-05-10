package com.csubb.dissertation.customautoscaler.integration.prometheus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusAPI {

    private final PrometheusClient prometheusClient;

    public Map.Entry<PrometheusQueryType, Double> getMetric(PrometheusQueryType queryType, String namespace, String scaledDeployment) {
        Double value = prometheusClient.getPrometheusMetrics(namespace, scaledDeployment, queryType);
        return new AbstractMap.SimpleEntry<>(queryType, value);
    }

    public List<String> getServicesFromNamespace(String namespace) {
        return prometheusClient.getServicesFromNamespace(namespace);
    }
}
