package com.csubb.dissertation.customautoscaler.infrastructure;

import com.csubb.dissertation.customautoscaler.algorithm.ScalingStrategy;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SimpleDeploymentScaler extends AbstractDeploymentScaler {

    @Autowired
    public SimpleDeploymentScaler(ScalingStrategy scalingStrategy, KubernetesClientService k8sClientService) {
        super(scalingStrategy, k8sClientService);
    }

    @Override
    public void doDynamicScaling(ScaledDeployment scaledDeployment) {
        // No dynamic scaling logic in this simple implementation
    }
}
