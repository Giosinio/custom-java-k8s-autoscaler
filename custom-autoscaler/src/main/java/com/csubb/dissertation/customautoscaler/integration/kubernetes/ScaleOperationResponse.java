package com.csubb.dissertation.customautoscaler.integration.kubernetes;

public record ScaleOperationResponse(
        boolean hasScaled,
        int updatedReplicaCount
) {
}
