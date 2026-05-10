package com.csubb.dissertation.customautoscaler.scaling.deployment;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.scaling.strategy.ScalingStrategy;
import com.csubb.dissertation.customautoscaler.integration.prometheus.PrometheusAPI;
import com.csubb.dissertation.customautoscaler.integration.prometheus.PrometheusQueryType;
import com.csubb.dissertation.customautoscaler.integration.kubernetes.KubernetesClientService;
import com.csubb.dissertation.customautoscaler.util.Util;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.AbstractMap;
import java.util.List;
import java.util.Objects;

import static com.csubb.dissertation.customautoscaler.util.Util.isStable;

@Slf4j
@Component
public class EnhancedDeploymentScaler extends AbstractDeploymentScaler {

    private static final int STABILITY_INTERVAL = 5;
    private static final double STABILITY_PERCENTAGE_THRESHOLD = 15d;

    private final PrometheusAPI prometheusAPI;

    public EnhancedDeploymentScaler(ScalingStrategy scalingStrategy, KubernetesClientService k8sClientService, PrometheusAPI prometheusAPI) {
        super(scalingStrategy, k8sClientService);
        this.prometheusAPI = prometheusAPI;
    }

    @Override
    @SneakyThrows
    public void doDynamicScaling(ScaledDeployment scaledDeployment) {
        List<Double> avgCpuPercentageEvolution = scaledDeployment.getByPrometheusQueryType(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE);
        if(!isStable(avgCpuPercentageEvolution, STABILITY_INTERVAL, STABILITY_PERCENTAGE_THRESHOLD)) {
            Double avgCpuUsageMillicores = prometheusAPI.getMetric(PrometheusQueryType.AVG_CPU_USAGE_MILLICORES, scaledDeployment.getNamespace(), scaledDeployment.getService()).getValue();
            if(Objects.isNull(avgCpuUsageMillicores)) {
                log.error("Failed to fetch average CPU usage for application {}, skipping this scaling iteration.", scaledDeployment.getService());
                return;
            }
            Double avgCpuUsagePercentage = Util.getAvgCpuUsagePercentage(avgCpuUsageMillicores, scaledDeployment.getResourceRequestCpuMillicores());
            scaledDeployment.addMetricValue(new AbstractMap.SimpleEntry<>(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE, avgCpuUsagePercentage));

            int updatedReplicaCount = scalingStrategy.calculateUpdatedReplicaCount(scaledDeployment);
            k8sClientService.scaleDeployment(scaledDeployment.getNamespace(), scaledDeployment.getService(), updatedReplicaCount);
        }
    }
}
