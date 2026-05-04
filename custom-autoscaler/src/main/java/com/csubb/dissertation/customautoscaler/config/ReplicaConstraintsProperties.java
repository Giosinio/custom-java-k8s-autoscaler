package com.csubb.dissertation.customautoscaler.config;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "custom-autoscaler.replica-constraints")
@Validated
public record ReplicaConstraintsProperties(
        @NotNull(message = "minReplicas cannot be null")
        @Positive(message = "minReplicas must be a positive number")
        Integer minReplicas,

        @NotNull(message = "maxReplicas cannot be null")
        @Positive(message = "maxReplicas must be a positive number")
        Integer maxReplicas
) {

}

