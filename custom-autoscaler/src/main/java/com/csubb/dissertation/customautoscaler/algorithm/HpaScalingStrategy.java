package com.csubb.dissertation.customautoscaler.algorithm;

import lombok.RequiredArgsConstructor;

/**
 * This scaling strategy will increase the number of replicas depending on the average CPU usage. For this strategy to
 * properly work, the value for minToleranceCpuPercentage must be greater than 20%, for scaling down logic.
 */
@RequiredArgsConstructor
public class HpaScalingStrategy implements ScalingStrategy {

    private final Double minToleranceCpuPercentage;
    private final Double maxToleranceCpuPercentage;

    @Override
    public int calculateReplicaChange(ScalingContext context) {
        int result = 0;
        Double lastCpuAverage = context.getAvgCpuPercentageEvolution().get(context.getAvgCpuPercentageEvolution().size() - 1);
        if(lastCpuAverage > maxToleranceCpuPercentage) {
            result = 1 + (int) Math.floor(lastCpuAverage / maxToleranceCpuPercentage);
        }
        else if (lastCpuAverage < minToleranceCpuPercentage) {
            result = -1 * (int) Math.floor(minToleranceCpuPercentage / lastCpuAverage);
        }

        return result;
    }
}
