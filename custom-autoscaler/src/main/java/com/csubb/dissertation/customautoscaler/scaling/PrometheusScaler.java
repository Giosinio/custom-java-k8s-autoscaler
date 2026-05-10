package com.csubb.dissertation.customautoscaler.scaling;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;
import com.csubb.dissertation.customautoscaler.scaling.deployment.DeploymentScaler;
import com.csubb.dissertation.customautoscaler.scaling.monitoring.AvgCpuUtilizationStabilityMonitor;
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
