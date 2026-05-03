package com.csubb.dissertation.customautoscaler.prometheus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@Getter
@RequiredArgsConstructor
public enum PrometheusQueryType {

    NAMESPACE_SERVICES(Function.identity()),
    AVG_CPU_USAGE_MILLICORES(value -> value * 1000),
    AVG_CPU_USAGE_PERCENTAGE(Function.identity()), //just for grouping, not implemented
    AVG_MEMORY_USAGE_MIB(value -> value / (1024 * 1024)),
    KUBE_POD_RESOURCE_REQUESTS_CPU_MILLICORES(value -> value * 1000);

    private final Function<Double, Double> valueConverter;
}
