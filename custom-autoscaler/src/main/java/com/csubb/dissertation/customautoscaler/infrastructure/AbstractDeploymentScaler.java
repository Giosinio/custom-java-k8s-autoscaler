package com.csubb.dissertation.customautoscaler.infrastructure;

import com.csubb.dissertation.customautoscaler.algorithm.ScalingStrategy;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
import com.csubb.dissertation.customautoscaler.service.ScaleOperationResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractDeploymentScaler implements DeploymentScaler {

    protected final ScalingStrategy scalingStrategy;
    protected final KubernetesClientService k8sClientService;

    @Override
    @SneakyThrows
    public boolean scale(ScaledDeployment scaledDeployment) {
        int updatedReplicaCount = scalingStrategy.calculateUpdatedReplicaCount(scaledDeployment);

        if(scaledDeployment.getReplicaCount() != updatedReplicaCount) {
            return false;
        }

        ScaleOperationResponse scaleOperationResponse = k8sClientService.scaleDeployment(scaledDeployment.getNamespace(), scaledDeployment.getService(), updatedReplicaCount);
        if(!scaleOperationResponse.hasScaled()) {
            return false;
        }

        int targetReplicas = scaleOperationResponse.updatedReplicaCount();
        doScaling(scaledDeployment, targetReplicas);

        return true;
    }

    @SneakyThrows
    private void doScaling(ScaledDeployment scaledDeployment, int targetReplicas) {
        while(true) {
            doDynamicScaling(scaledDeployment);
            if(k8sClientService.checkAllPodsAreReadyForDeployment(scaledDeployment, targetReplicas)) {
                log.info("Scaling operation completed successfully. New replica count is now stable.");
                break;
            }
            log.info("Waiting for pods to be ready after scaling operation...");
            Thread.sleep(1000 * 2);
        }
        log.info("Scaling operation completed. Now monitoring CPU usage for stability...");
    }

    public abstract void doDynamicScaling(ScaledDeployment scaledDeployment);
}
