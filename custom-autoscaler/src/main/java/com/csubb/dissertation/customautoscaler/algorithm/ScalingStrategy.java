package com.csubb.dissertation.customautoscaler.algorithm;

public interface ScalingStrategy {

    int calculateReplicaChange(ScalingContext context);
}
