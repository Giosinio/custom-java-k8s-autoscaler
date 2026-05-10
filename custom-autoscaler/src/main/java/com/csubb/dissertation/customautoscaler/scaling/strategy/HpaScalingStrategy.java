package com.csubb.dissertation.customautoscaler.scaling.strategy;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.integration.prometheus.PrometheusQueryType;
import com.csubb.dissertation.customautoscaler.util.Util;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

/**
 * This scaling strategy will increase the number of replicas depending on the average CPU usage. For this strategy to
 * properly work, the value for minToleranceCpuPercentage must be greater than 20%, for scaling down logic.
 */
@RequiredArgsConstructor
public class HpaScalingStrategy implements ScalingStrategy {

    private final Double minToleranceCpuPercentage;
    private final Double maxToleranceCpuPercentage;

    @PostConstruct
    public void init() {
        assert minToleranceCpuPercentage <= maxToleranceCpuPercentage;
    }

    @Override
    public int calculateUpdatedReplicaCount(ScaledDeployment scaledDeployment) {
        int deltaReplica = 0;
        List<Double> avgCpuUsagePercentageEvolution = scaledDeployment.getByPrometheusQueryType(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE)
                .stream()
                .filter(Objects::nonNull)
                .toList();

        Double lastCpuAverage = avgCpuUsagePercentageEvolution.get(avgCpuUsagePercentageEvolution.size() - 1);
        if(lastCpuAverage > maxToleranceCpuPercentage) {
            deltaReplica = 1 + (int) Math.floor(lastCpuAverage / maxToleranceCpuPercentage);
        }
        else if (lastCpuAverage < minToleranceCpuPercentage) {
            deltaReplica = -1 * (int) Math.floor(minToleranceCpuPercentage / lastCpuAverage);
        }

        return Util.clampReplicas(scaledDeployment.getReplicaCount() + deltaReplica);
    }
}
