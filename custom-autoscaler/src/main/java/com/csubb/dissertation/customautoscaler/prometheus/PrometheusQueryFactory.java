package com.csubb.dissertation.customautoscaler.prometheus;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class PrometheusQueryFactory {

    public static String createQuery(PrometheusQueryType queryType, String namespace, String podNameSuffix) {
        return switch (queryType) {
            case AVG_CPU_USAGE_CORES -> String.format(
                    "avg(rate(container_cpu_usage_seconds_total{namespace=\"%s\", pod=~\"%s-.*\", container!=\"POD\"}[1m]))",
                    namespace, podNameSuffix
            );
            case AVG_MEMORY_USAGE_BYTES -> String.format(
                    "avg(container_memory_usage_bytes{namespace=\"%s\", pod=~\"%s-.*\", container!=\"POD\"})",
                    namespace, podNameSuffix
            );
            case KUBE_POD_RESOURCE_REQUESTS_CPU_CORES -> String.format(
                    "avg(kube_pod_container_resource_requests{namespace=\"%s\", pod=~\"%s-.*\", resource=\"cpu\"})",
                    namespace, podNameSuffix
            );
        };
    }
}
