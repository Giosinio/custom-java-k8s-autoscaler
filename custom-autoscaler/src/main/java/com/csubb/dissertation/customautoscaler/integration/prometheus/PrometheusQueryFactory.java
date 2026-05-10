package com.csubb.dissertation.customautoscaler.integration.prometheus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PrometheusQueryFactory {

    public static String createQuery(PrometheusQueryType queryType, String ... parameters) {
        return switch (queryType) {
            case NAMESPACE_SERVICES -> String.format(
                    "kube_service_info{namespace=\"%s\"}",
                    parameters[0]
            );
            case AVG_CPU_USAGE_MILLICORES -> String.format(
                    "avg(rate(container_cpu_usage_seconds_total{namespace=\"%s\", pod=~\"%s-.*\", container!=\"POD\"}[1m]))",
                    parameters[0], parameters[1]
            );
            case AVG_MEMORY_USAGE_MIB -> String.format(
                    "avg(container_memory_usage_bytes{namespace=\"%s\", pod=~\"%s-.*\", container!=\"POD\"})",
                    parameters[0], parameters[1]
            );
            case KUBE_POD_RESOURCE_REQUESTS_CPU_MILLICORES -> String.format(
                    "avg(kube_pod_container_resource_requests{namespace=\"%s\", pod=~\"%s-.*\", resource=\"cpu\"})",
                    parameters[0], parameters[1]
            );
            case AVG_CPU_USAGE_PERCENTAGE -> throw new NotImplementedException();
        };
    }
}
