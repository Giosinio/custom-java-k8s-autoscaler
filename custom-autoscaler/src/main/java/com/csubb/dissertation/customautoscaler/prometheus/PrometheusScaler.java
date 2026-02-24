package com.csubb.dissertation.customautoscaler.prometheus;

import com.csubb.dissertation.customautoscaler.algorithm.ScalingContext;
import com.csubb.dissertation.customautoscaler.algorithm.ScalingStrategy;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
import com.csubb.dissertation.customautoscaler.service.ScaleOperationResponse;
import io.kubernetes.client.openapi.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusScaler {

    private final ScalingStrategy scalingStrategy;
    private final KubernetesClientService k8sClientService;
    private final AvgCpuUtilizationStabilityMonitor cpuStabilityMonitor;

    public void performScaling(ScalingContext scalingContext) throws ApiException, InterruptedException {
        int deltaReplicas = scalingStrategy.calculateReplicaChange(scalingContext);
        if(deltaReplicas != 0) {
            ScaleOperationResponse scaleOperationResponse = k8sClientService.scaleDeployment(scalingContext.getNamespace(), scalingContext.getScaledDeployment(), deltaReplicas);

            if(scaleOperationResponse.hasScaled()) {
                int targetReplicas = scaleOperationResponse.updatedNumberOfReplicas();
                while(true) {
                    if(k8sClientService.checkAllPodsAreReadyForDeployment(scalingContext, targetReplicas)) {
                        log.info("Scaling operation completed successfully. New replica count is now stable.");
                        break;
                    }
                    log.info("Waiting for pods to be ready after scaling operation...");
                    Thread.sleep(1000 * 2);
                }
                log.info("Scaling operation completed. Now monitoring CPU usage for stability...");
                cpuStabilityMonitor.waitForSystemStability(scalingContext);
            }
        }
    }
}
