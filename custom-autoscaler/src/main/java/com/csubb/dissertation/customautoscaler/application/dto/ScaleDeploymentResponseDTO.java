package com.csubb.dissertation.customautoscaler.application.dto;


import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO used to send the information we want after scaling a deployment.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ScaleDeploymentResponseDTO(
        String deploymentName,
        Integer previousReplicaCount,
        Integer currentReplicaCount,
        boolean success,
        String message
) {

}
