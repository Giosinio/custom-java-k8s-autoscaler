package com.csubb.dissertation.customautoscaler.domain;

import com.csubb.dissertation.customautoscaler.integration.prometheus.PrometheusQueryType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class ScaledDeployment {

    private final String namespace;

    private final String service;

    private final Double resourceRequestCpuMillicores;

    private final Map<PrometheusQueryType, List<Double>> metricsEvolutionByQuery = new HashMap<>();

    @Setter
    private Integer replicaCount;

    public List<Double> getByPrometheusQueryType(PrometheusQueryType queryType) {
        return metricsEvolutionByQuery.get(queryType);
    }

    public void addMetricValue(Map.Entry<PrometheusQueryType, Double> entry) {
        metricsEvolutionByQuery.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
    }
}
