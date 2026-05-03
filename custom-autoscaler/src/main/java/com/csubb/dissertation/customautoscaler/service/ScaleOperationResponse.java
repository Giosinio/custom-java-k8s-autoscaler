package com.csubb.dissertation.customautoscaler.service;

public record ScaleOperationResponse(
        boolean hasScaled,
        int updatedReplicaCount
) {
}
