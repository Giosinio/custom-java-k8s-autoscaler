package com.csubb.dissertation.customautoscaler.scheduler;

import com.csubb.dissertation.customautoscaler.algorithm.ScalingContext;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusClient;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusQueryType;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusScaler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.csubb.dissertation.customautoscaler.util.Util.formatNullableDouble;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoscaleScheduler {

    private static Double resourceRequestCpuCores;

    @Value("${prometheus.namespace}")
    private String namespace;

    @Value("${prometheus.scaled-deployment}")
    private String scaledDeployment;

    private final PrometheusClient prometheusClient;

    private final PrometheusScaler prometheusScaler;

    private final List<Double> avgCpuPercentageEvolution = new ArrayList<>();

    @PostConstruct
    private void initializeAutoscaleScheduler() {
        if(Objects.nonNull(resourceRequestCpuCores)) {
            return;
        }

        resourceRequestCpuCores = prometheusClient.getPrometheusMetrics(namespace, scaledDeployment, PrometheusQueryType.KUBE_POD_RESOURCE_REQUESTS_CPU_CORES);
        if(Objects.isNull(resourceRequestCpuCores)) {
            log.error("Failed to fetch resource request CPU cores for application {}, skipping metrics polling", scaledDeployment);
            System.exit(1);
        }
        log.info("Fetched resource request CPU cores for application {}: {} cores", scaledDeployment, String.format("%.2f", resourceRequestCpuCores));
    }

    @Scheduled(fixedRateString = "${metrics.poll.interval-ms:5000}")
    public void pollMetrics() {
        try {
             Double avgCpuUsage = prometheusClient.getPrometheusMetrics(namespace, scaledDeployment, PrometheusQueryType.AVG_CPU_USAGE_CORES);
             Double avgCpuUsagePercentage = Objects.isNull(avgCpuUsage) ? null : avgCpuUsage / resourceRequestCpuCores * 100;
             Double avgMemoryUsage = prometheusClient.getPrometheusMetrics(namespace, scaledDeployment, PrometheusQueryType.AVG_MEMORY_USAGE_BYTES);

             log.info("Average CPU Usage: {} millicores | CPU Usage percentage: {}% | Average Memory Usage: {} MiB",
                     formatNullableDouble(avgCpuUsage, value -> value * 1000),
                     formatNullableDouble(avgCpuUsagePercentage, Function.identity()),
                     formatNullableDouble(avgMemoryUsage, value -> value / (1024 * 1024))
             );

            avgCpuPercentageEvolution.add(avgCpuUsagePercentage);
            prometheusScaler.performScaling(ScalingContext.builder()
                    .namespace(namespace)
                    .scaledDeployment(scaledDeployment)
                    .resourceRequestCpuCores(resourceRequestCpuCores)
                    .avgCpuPercentageEvolution(avgCpuPercentageEvolution)
                    .build()
            );
        } catch (Exception e) {
            log.error("Failed to poll metrics", e);
        }
    }
}
