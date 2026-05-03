package com.csubb.dissertation.customautoscaler.util;

import jakarta.annotation.Nullable;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class Util {

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
}
