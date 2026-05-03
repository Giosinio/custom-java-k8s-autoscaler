package com.csubb.dissertation.customautoscaler.scheduler;

import com.csubb.dissertation.customautoscaler.config.CustomAutoscalerProperties;
import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusAPI;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusQueryType;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusScaler;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
import com.csubb.dissertation.customautoscaler.util.Util;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.csubb.dissertation.customautoscaler.util.Util.formatNullableDouble;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoscaleScheduler {

    private final CustomAutoscalerProperties customAutoscalerProperties;

    private final PrometheusAPI prometheusAPI;

    private final PrometheusScaler prometheusScaler;

    private final KubernetesClientService k8sClientService;

    private Map<String, ScaledDeployment> scaledDeploymentsMap;

    @Value("${custom-autoscaler.namespace}")
    private String namespace;

    @PostConstruct
    private void initializeAutoscaleScheduler() {
        if(Objects.equals(customAutoscalerProperties.scaleAllFromNamespace(), Boolean.TRUE)) {
            var services = prometheusAPI.getServicesFromNamespace(namespace);
            if (services == null || services.isEmpty()) {
                log.warn("No services found in namespace {}, autoscaling will be disabled until services appear", namespace);
                scaledDeploymentsMap = Map.of();
                return;
            }
            scaledDeploymentsMap = services.stream()
                    .filter(service -> !customAutoscalerProperties.excludedServicesFromNamespace().contains(service))
                    .map(mappingFunction())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ScaledDeployment::getService, Function.identity()));
            log.info("Scaling all services from namespace {}, the list is: {}", namespace, scaledDeploymentsMap.keySet());
        }
        else {
            var targetServices = customAutoscalerProperties.targetServices();
            if (targetServices == null || targetServices.isEmpty()) {
                log.warn("No target services configured for namespace {}, autoscaling will be disabled", namespace);
                scaledDeploymentsMap = Map.of();
                return;
            }
            log.info("Scaling only the following services from namespace {}: {}", namespace, targetServices);
            scaledDeploymentsMap = targetServices.stream()
                    .map(mappingFunction())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(ScaledDeployment::getService, Function.identity()));
        }
    }

    private Function<String, ScaledDeployment> mappingFunction() {
        return (service) -> {
            Map.Entry<PrometheusQueryType, Double> cpuRequestMetric = prometheusAPI.getMetric(PrometheusQueryType.KUBE_POD_RESOURCE_REQUESTS_CPU_MILLICORES, namespace, service);
            Double resourceRequestCpuMillicores = cpuRequestMetric == null ? null : cpuRequestMetric.getValue();
            if (Objects.isNull(resourceRequestCpuMillicores)) {
                log.warn("Skipping service {} in namespace {} because CPU request metric {} is unavailable", service, namespace, PrometheusQueryType.KUBE_POD_RESOURCE_REQUESTS_CPU_MILLICORES);
                return null;
            }
            return new ScaledDeployment(namespace, service, resourceRequestCpuMillicores);
        };
    }

    @Scheduled(fixedRateString = "${metrics.poll.interval-ms:5000}")
    public void pollMetrics() {
        if (scaledDeploymentsMap == null || scaledDeploymentsMap.isEmpty()) {
            log.info("No scaled deployments configured for namespace {}, skipping this polling iteration", namespace);
            return;
        }
        scaledDeploymentsMap.values().forEach(this::doScalingForDeployment);
    }

    private void doScalingForDeployment(ScaledDeployment scaledDeployment) {
        String service = scaledDeployment.getService();
        try {
            Map.Entry<PrometheusQueryType, Double> avgCpuUsageMillicores = prometheusAPI.getMetric(PrometheusQueryType.AVG_CPU_USAGE_MILLICORES, namespace, service);
            Map.Entry<PrometheusQueryType, Double> avgCpuUsagePercentage = new AbstractMap.SimpleEntry<>(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE, Util.getAvgCpuUsagePercentage(avgCpuUsageMillicores.getValue(), scaledDeployment.getResourceRequestCpuMillicores()));
            Map.Entry<PrometheusQueryType, Double> avgMemoryUsageMiB = prometheusAPI.getMetric(PrometheusQueryType.AVG_MEMORY_USAGE_MIB, namespace, service);

            log.info("Average CPU Usage: {} millicores | CPU Usage percentage: {}% | Average Memory Usage: {} MiB",
                    formatNullableDouble(avgCpuUsageMillicores.getValue()),
                    formatNullableDouble(avgCpuUsagePercentage.getValue()),
                    formatNullableDouble(avgMemoryUsageMiB.getValue())
            );

            scaledDeployment.addMetricValue(avgCpuUsageMillicores);
            scaledDeployment.addMetricValue(avgCpuUsagePercentage);
            scaledDeployment.addMetricValue(avgMemoryUsageMiB);

            scaledDeployment.setReplicaCount(k8sClientService.getDeploymentReplicaCount(namespace, service));

            prometheusScaler.performScaling(scaledDeployment);
        } catch (Exception e) {
            log.error("Failed to poll metrics", e);
        }
    }
}
