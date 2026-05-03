package com.csubb.dissertation.customautoscaler.algorithm;

import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.prometheus.PrometheusQueryType;
import lombok.RequiredArgsConstructor;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class ProportionStrategy implements ScalingStrategy {

    private static double targetPodsNumber = 2d;

    private final Double minToleranceCpuPercentage;
    private final Double maxToleranceCpuPercentage;

    @PostConstruct
    public void init() {
        assert minToleranceCpuPercentage <= maxToleranceCpuPercentage;
    }

    @Override
    public int calculateUpdatedReplicaCount(ScaledDeployment scaledDeployment) {
        List<Double> avgCpuSamples = scaledDeployment.getByPrometheusQueryType(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE).stream()
                .filter(Objects::nonNull)
                .toList();
        if(avgCpuSamples.size() <= 3) {
            return scaledDeployment.getReplicaCount(); //not enough data to determine the trend of avg CPU evolution
        }
        Double latestAvgCpu = avgCpuSamples.get(avgCpuSamples.size() - 1);
        Double secondLatestAvgCpu =  avgCpuSamples.get(avgCpuSamples.size() - 2);

        if(minToleranceCpuPercentage < latestAvgCpu && latestAvgCpu < maxToleranceCpuPercentage) {
            targetPodsNumber = scaledDeployment.getReplicaCount();
        }

        // this is the case when the avgCpu increases and exceeds maxToleranceCpuPercentage
        if(latestAvgCpu > maxToleranceCpuPercentage && latestAvgCpu > secondLatestAvgCpu) {
            targetPodsNumber = targetPodsNumber * (latestAvgCpu / secondLatestAvgCpu);
        }

        // this is the case when the avgCpu decreases and goes below minToleranceCpuPercentage
        if(latestAvgCpu < minToleranceCpuPercentage && latestAvgCpu < secondLatestAvgCpu) {
            targetPodsNumber = targetPodsNumber / (secondLatestAvgCpu / latestAvgCpu);
        }

        return (int) targetPodsNumber;
    }

}
