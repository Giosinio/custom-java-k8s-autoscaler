package com.csubb.dissertation.customautoscaler.prometheus;

import com.csubb.dissertation.customautoscaler.algorithm.ScalingContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvgCpuUtilizationStabilityMonitor {

    private static final Integer STABILITY_INTERVAL = 10;
    private static final Double STABILITY_PERCENTAGE_THRESHOLD = 15d; // 5% change in CPU usage is considered unstable

    private final PrometheusClient prometheusClient;

    public void waitForSystemStability(ScalingContext scalingContext) throws InterruptedException {
        int initialCpuEvolutionSize = scalingContext.getAvgCpuPercentageEvolution().size();
        while(true) {
            Thread.sleep(1000 * 2);
            if(scalingContext.getAvgCpuPercentageEvolution().size() - initialCpuEvolutionSize > STABILITY_INTERVAL && checkStability(scalingContext)) {
                log.info("System has stabilized after scaling operation for application {}.", scalingContext.getScaledDeployment());
                break;
            }

            scalingContext.getAvgCpuPercentageEvolution().add(calculateAvgCpuUsagePercentage(scalingContext));
        }
    }

    private Double calculateAvgCpuUsagePercentage(ScalingContext scalingContext) {
        Double avgCpuUsage = prometheusClient.getPrometheusMetrics(scalingContext.getNamespace(), scalingContext.getScaledDeployment(), PrometheusQueryType.AVG_CPU_USAGE_CORES);
        if(Objects.isNull(avgCpuUsage)) {
            log.info("Failed to fetch average CPU usage for application {}, skipping this stability check iteration.", scalingContext.getScaledDeployment());
            return null;
        }

        Double avgCpuUsagePercentage = avgCpuUsage / scalingContext.getResourceRequestCpuCores() * 100;
        log.info("CPU Usage percentage: {}%", String.format("%.2f", avgCpuUsagePercentage));

        return avgCpuUsagePercentage;
    }

    private static boolean checkStability(ScalingContext scalingContext) {
        List<Double> avgCpuEvolutionList = scalingContext.getAvgCpuPercentageEvolution();
        List<Double> latestAvgCpuEvolution = avgCpuEvolutionList.subList(avgCpuEvolutionList.size() - STABILITY_INTERVAL - 1, avgCpuEvolutionList.size());
        Double minAvgCpuUsage = latestAvgCpuEvolution.stream().min(Double::compareTo).orElse(null);
        Double maxAvgCpuUsage = latestAvgCpuEvolution.stream().max(Double::compareTo).orElse(null);

        return Objects.requireNonNull(maxAvgCpuUsage) - Objects.requireNonNull(minAvgCpuUsage) <= STABILITY_PERCENTAGE_THRESHOLD;
    }
}
