package com.csubb.dissertation.customautoscaler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Set;

@ConfigurationProperties(prefix = "custom-autoscaler")
public record CustomAutoscalerProperties(
        String namespace,
        List<String> targetServices,
        Boolean scaleAllFromNamespace,
        Set<String> excludedServicesFromNamespace
) {

}
