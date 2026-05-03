package com.csubb.dissertation.customautoscaler.algorithm;

import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class MovingAverageStrategy implements ScalingStrategy {

    private static final int SCALE_UP_COOLDOWN_SECONDS = 15;
    private static final int SCALE_DOWN_COOLDOWN_SECONDS = 60;
    private static LocalDateTime LAST_SCALE_UP_TIME = LocalDateTime.now().minusMinutes(100);
    private static LocalDateTime LAST_SCALE_DOWN_TIME = LocalDateTime.now().minusMinutes(100);

    private final Double targetAvgCpu;
    private final Double varianceThreshold;
    private final Double stabilityThreshold;
    private final Double volatilityMultiplier;

    @Override
    public int calculateUpdatedReplicaCount(ScaledDeployment scaledDeployment) {
        List<Double> list = scaledDeployment.getByPrometheusQueryType(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE).stream()
                .filter(Objects::nonNull)
                .toList();
        if(list.size() <= 3) {
            return scaledDeployment.getReplicaCount(); //not enough data to determine the trend of avg CPU evolution
        }

        Double currentAvgCpu = list.get(list.size() - 1);
        double avg = list.stream().collect(Collectors.averagingDouble(Double::doubleValue)) / list.size();
        double variance = calculateVariance(list);
        int updatedReplicaCount;

        boolean isStable = variance < varianceThreshold && Math.abs(currentAvgCpu - avg) <= stabilityThreshold;
        if(!isStable) {
            double scaleFactor = (currentAvgCpu / targetAvgCpu) * (1 + variance * volatilityMultiplier / 100);
            updatedReplicaCount = (int) Math.ceil(scaledDeployment.getReplicaCount() * scaleFactor);
        }
        else {
            //conservative scaling
            if(currentAvgCpu > targetAvgCpu * 1.1) {
                updatedReplicaCount = scaledDeployment.getReplicaCount() + 1;
            } else if(currentAvgCpu < targetAvgCpu * 0.8) {
                updatedReplicaCount = scaledDeployment.getReplicaCount() - 1;
            } else {
                updatedReplicaCount = scaledDeployment.getReplicaCount();
            }
        }

        if(updatedReplicaCount > scaledDeployment.getReplicaCount()) {
            if(isCooldown(updatedReplicaCount, scaledDeployment)) {
                log.info("Scale up cooldown in effect. Skipping scaling operation. Current replica count: {}, Updated replica count: {}", scaledDeployment.getReplicaCount(), updatedReplicaCount);
                updatedReplicaCount = scaledDeployment.getReplicaCount();
            } else {
                updateCooldown(updatedReplicaCount, scaledDeployment);
            }
        } else if(updatedReplicaCount < scaledDeployment.getReplicaCount()) {
            if(isCooldown(updatedReplicaCount, scaledDeployment)) {
                log.info("Scale down cooldown in effect. Skipping scaling operation. Current replica count: {}, Updated replica count: {}", scaledDeployment.getReplicaCount(), updatedReplicaCount);
                updatedReplicaCount = scaledDeployment.getReplicaCount();
            } else {
                updateCooldown(updatedReplicaCount, scaledDeployment);
            }
        } else {
            log.info("Replica count is stable. No scaling operation needed. Current replica count: {}", scaledDeployment.getReplicaCount());
        }

        return updatedReplicaCount;
    }

    private static Double calculateVariance(List<Double> list) {
        Double meanAvgCpuUsage = list.stream().mapToDouble(Double::doubleValue).sum() / list.size();
        return list.stream().map(x -> Math.pow(x - meanAvgCpuUsage, 2)).mapToDouble(Double::doubleValue).sum() / list.size();
    }

    private static boolean isCooldown(int updatedReplicaCount, ScaledDeployment scaledDeployment) {
        ScalingDirection scalingDirection = determineScalingDirection(updatedReplicaCount, scaledDeployment);
        if(scalingDirection == ScalingDirection.UP) {
            return LocalDateTime.now().minusSeconds(SCALE_UP_COOLDOWN_SECONDS).isBefore(LAST_SCALE_UP_TIME);
        } else if(scalingDirection == ScalingDirection.DOWN) {
            return LocalDateTime.now().minusSeconds(SCALE_DOWN_COOLDOWN_SECONDS).isBefore(LAST_SCALE_DOWN_TIME);
        }
        return false; // shouldn't reach this line ever
    }

    private static ScalingDirection determineScalingDirection(int updatedReplicaCount, ScaledDeployment scaledDeployment) {
        if(updatedReplicaCount >  scaledDeployment.getReplicaCount()) {
            return ScalingDirection.UP;
        } else if (updatedReplicaCount <  scaledDeployment.getReplicaCount()) {
            return ScalingDirection.DOWN;
        }
        return  ScalingDirection.STABLE;
    }

    private static void updateCooldown(int updatedReplicaCount, ScaledDeployment scaledDeployment) {
        ScalingDirection scalingDirection = determineScalingDirection(updatedReplicaCount, scaledDeployment);
        if(scalingDirection == ScalingDirection.UP) {
            LAST_SCALE_UP_TIME = LocalDateTime.now();
        } else if(scalingDirection == ScalingDirection.DOWN) {
            LAST_SCALE_DOWN_TIME = LocalDateTime.now();
        }
    }
}
