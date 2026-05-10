package com.csubb.dissertation.customautoscaler.application;

import com.csubb.dissertation.customautoscaler.application.dto.ScaleDeploymentDTO;
import com.csubb.dissertation.customautoscaler.application.dto.ScaleDeploymentResponseDTO;
import com.csubb.dissertation.customautoscaler.integration.kubernetes.KubernetesClientService;
import io.kubernetes.client.openapi.ApiException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-autoscaler")
public class CustomAutoscalerController {

    private final KubernetesClientService k8sClientService;

    @PostMapping("/scale")
    public ResponseEntity<ScaleDeploymentResponseDTO> scaleDeployment(@Valid @RequestBody ScaleDeploymentDTO scaleDeploymentDTO) throws ApiException {
        String deploymentName = scaleDeploymentDTO.deploymentName();
        String namespace = Objects.nonNull(scaleDeploymentDTO.namespace()) ? scaleDeploymentDTO.namespace() : "default";
        int currentReplicaCount = k8sClientService.getDeploymentReplicaCount(namespace, deploymentName);

        try {
            Integer updatedReplicaCount = k8sClientService.scaleDeployment(namespace, deploymentName, scaleDeploymentDTO.updatedReplicaCount()).updatedReplicaCount();

            return ResponseEntity.ok(new ScaleDeploymentResponseDTO(deploymentName, currentReplicaCount, updatedReplicaCount, true, null));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScaleDeploymentResponseDTO(deploymentName, -1, currentReplicaCount, false, "Kubernetes API error: " + e.getResponseBody()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScaleDeploymentResponseDTO(deploymentName, -1, currentReplicaCount, false, "Unexpected error: " + e.getMessage()));
        }
    }
}
