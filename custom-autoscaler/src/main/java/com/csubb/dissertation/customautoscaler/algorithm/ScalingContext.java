package com.csubb.dissertation.customautoscaler.algorithm;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ScalingContext {

    private final String namespace;

    private final String scaledDeployment;

    private final Double resourceRequestCpuCores;

    private final List<Double> avgCpuPercentageEvolution;
}
