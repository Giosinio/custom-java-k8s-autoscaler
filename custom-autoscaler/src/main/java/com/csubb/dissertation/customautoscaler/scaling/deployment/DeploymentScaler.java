package com.csubb.dissertation.customautoscaler.scaling.deployment;

import com.csubb.dissertation.customautoscaler.domain.ScaledDeployment;

public interface DeploymentScaler {

    boolean scale(ScaledDeployment scaledDeployment);
}
