package com.csubb.dissertation.customautoscaler.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * DTO used to send the new number of replicas we want to scale to for a deployment.
 * @param deploymentName - The name of the deployment to be scaled
 * @param namespace - The namespace of the deployment (will assume default namespace if null)
 * @param updatedNumberOfReplicas - The new number of replicas for the deployment
 */
public record ScaleDeploymentDTO(
        @NotEmpty String deploymentName,
        @Nullable String namespace,
        @NotNull Integer updatedNumberOfReplicas
) {

}
