package com.csubb.dissertation.customautoscaler.prometheus;

import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.infrastructure.DeploymentScaler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrometheusScaler {

    private final DeploymentScaler deploymentScaler;
    private final AvgCpuUtilizationStabilityMonitor cpuStabilityMonitor;

    public void performScaling(ScaledDeployment scaledDeployment) throws InterruptedException {
        boolean replicaCountChanged = deploymentScaler.scale(scaledDeployment);
        if (replicaCountChanged) {
            cpuStabilityMonitor.waitForSystemStability(scaledDeployment);
        }
    }
}
