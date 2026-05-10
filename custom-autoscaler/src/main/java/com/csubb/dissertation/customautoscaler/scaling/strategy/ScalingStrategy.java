package com.csubb.dissertation.customautoscaler.scaling.strategy;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;

public interface ScalingStrategy {

    int calculateUpdatedReplicaCount(ScaledDeployment scaledDeployment);
}
