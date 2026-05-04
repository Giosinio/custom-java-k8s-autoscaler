package com.csubb.dissertation.customautoscaler.util;

import com.csubb.dissertation.customautoscaler.config.ReplicaConstraintsProperties;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class Util {

    private static ReplicaConstraintsProperties replicaConstraintsProperties;

    @Autowired
    public Util(ReplicaConstraintsProperties replicaConstraintsProperties) {
        Util.replicaConstraintsProperties = replicaConstraintsProperties;
        assert replicaConstraintsProperties.minReplicas() <= replicaConstraintsProperties.maxReplicas() : "minReplicas should be less than or equal to maxReplicas";
    }

    public static String formatNullableDouble(@Nullable Double value) {
        return Objects.isNull(value) ? "null" : String.format("%.2f", value);
    }

    public static boolean isStable(List<Double> values, int stabilityInterval, double stabilityPercentageThreshold) {
        List<Double> latestAvgCpuEvolution = values.subList(values.size() - stabilityInterval - 1, values.size());
        Double minAvgCpuUsage = latestAvgCpuEvolution.stream().min(Double::compareTo).orElse(null);
        Double maxAvgCpuUsage = latestAvgCpuEvolution.stream().max(Double::compareTo).orElse(null);

        return Objects.requireNonNull(maxAvgCpuUsage) - Objects.requireNonNull(minAvgCpuUsage) <= stabilityPercentageThreshold;
    }

    public static Double getAvgCpuUsagePercentage(@Nullable Double avgCpuUsage, Double resourceRequestCpuCores) {
        return Objects.isNull(avgCpuUsage) ? null : avgCpuUsage / resourceRequestCpuCores * 100;
    }

    public static Integer clampReplicas(Integer updatedReplicasNumber) {
        Integer minReplicas = replicaConstraintsProperties.minReplicas();
        Integer maxReplicas = replicaConstraintsProperties.maxReplicas();

        if(updatedReplicasNumber < minReplicas) {
            return minReplicas;
        } else if (updatedReplicasNumber > maxReplicas) {
            return maxReplicas;
        }
        return updatedReplicasNumber;
    }
}
