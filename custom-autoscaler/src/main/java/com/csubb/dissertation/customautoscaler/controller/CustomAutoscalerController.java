package com.csubb.dissertation.customautoscaler.controller;

import com.csubb.dissertation.customautoscaler.dto.ScaleDeploymentDTO;
import com.csubb.dissertation.customautoscaler.dto.ScaleDeploymentResponseDTO;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
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

    private final KubernetesClientService kubernetesClientService;

    @PostMapping("/scale")
    public ResponseEntity<ScaleDeploymentResponseDTO> scaleDeployment(@Valid @RequestBody ScaleDeploymentDTO scaleDeploymentDTO) {
        String deploymentName = scaleDeploymentDTO.deploymentName();
        String namespace = Objects.nonNull(scaleDeploymentDTO.namespace()) ? scaleDeploymentDTO.namespace() : "default";
        int updatedReplicas = scaleDeploymentDTO.updatedNumberOfReplicas();

        try {
            Integer prevReplicas = kubernetesClientService.scaleDeployment(namespace, deploymentName, updatedReplicas).updatedNumberOfReplicas();

            return ResponseEntity.ok(new ScaleDeploymentResponseDTO(deploymentName, prevReplicas, updatedReplicas, true, null));
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScaleDeploymentResponseDTO(deploymentName, -1, updatedReplicas, false, "Kubernetes API error: " + e.getResponseBody()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ScaleDeploymentResponseDTO(deploymentName, -1, updatedReplicas, false, "Unexpected error: " + e.getMessage()));
        }
    }
}
