package com.csubb.dissertation.customautoscaler.infrastructure;

public interface DeploymentScaler {

    boolean scale(ScaledDeployment scaledDeployment);
}
