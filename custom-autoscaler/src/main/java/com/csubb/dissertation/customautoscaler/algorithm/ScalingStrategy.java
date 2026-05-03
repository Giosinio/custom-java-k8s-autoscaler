package com.csubb.dissertation.customautoscaler.algorithm;

import com.csubb.dissertation.customautoscaler.infrastructure.ScaledDeployment;

public interface ScalingStrategy {

    int calculateUpdatedReplicaCount(ScaledDeployment scaledDeployment);
}
