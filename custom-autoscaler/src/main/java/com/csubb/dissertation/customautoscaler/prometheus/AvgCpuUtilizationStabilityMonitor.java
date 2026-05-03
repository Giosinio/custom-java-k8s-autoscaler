package com.csubb.dissertation.customautoscaler.prometheus;

import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.csubb.dissertation.customautoscaler.util.Util.isStable;

@Slf4j
@Component
@RequiredArgsConstructor
public class AvgCpuUtilizationStabilityMonitor {

    private static final Integer STABILITY_INTERVAL = 10;
    private static final Double STABILITY_PERCENTAGE_THRESHOLD = 15d; // 15% change in CPU usage is considered unstable

    private final PrometheusClient prometheusClient;

    public void waitForSystemStability(ScaledDeployment scaledDeployment) throws InterruptedException {
        List<Double> avgCpuPercentageEvolution = scaledDeployment.getByPrometheusQueryType(PrometheusQueryType.AVG_CPU_USAGE_PERCENTAGE);
        int initialCpuEvolutionSize = avgCpuPercentageEvolution.size();
        while(true) {
            Thread.sleep(1000 * 2);
            if(avgCpuPercentageEvolution.size() - initialCpuEvolutionSize > STABILITY_INTERVAL
                    && isStable(avgCpuPercentageEvolution, STABILITY_INTERVAL, STABILITY_PERCENTAGE_THRESHOLD)) {
                log.info("System has stabilized after scaling operation for application {}.", scaledDeployment.getService());
                break;
            }

            avgCpuPercentageEvolution.add(calculateAvgCpuUsagePercentage(scaledDeployment));
        }
    }

    private Double calculateAvgCpuUsagePercentage(ScaledDeployment scaledDeployment) {
        Double avgCpuUsageMillicores = prometheusClient.getPrometheusMetrics(scaledDeployment.getNamespace(), scaledDeployment.getService(), PrometheusQueryType.AVG_CPU_USAGE_MILLICORES);
        if(Objects.isNull(avgCpuUsageMillicores)) {
            log.info("Failed to fetch average CPU usage for application {}, skipping this stability check iteration.", scaledDeployment.getService());
            return null;
        }

        Double avgCpuUsagePercentage = avgCpuUsageMillicores / scaledDeployment.getResourceRequestCpuMillicores() * 100;
        log.info("CPU Usage percentage: {}%", String.format("%.2f", avgCpuUsagePercentage));

        return avgCpuUsagePercentage;
    }
}
