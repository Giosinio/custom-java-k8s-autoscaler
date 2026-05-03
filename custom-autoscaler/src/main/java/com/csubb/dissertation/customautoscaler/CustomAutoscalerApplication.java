package com.csubb.dissertation.customautoscaler;

import com.csubb.dissertation.customautoscaler.algorithm.HpaScalingStrategy;
import com.csubb.dissertation.customautoscaler.algorithm.ScalingStrategy;
import com.csubb.dissertation.customautoscaler.infrastructure.DeploymentScaler;
import com.csubb.dissertation.customautoscaler.infrastructure.SimpleDeploymentScaler;
import com.csubb.dissertation.customautoscaler.service.KubernetesClientService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan(value = "com.csubb.dissertation.customautoscaler.config")
public class CustomAutoscalerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomAutoscalerApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ScalingStrategy scalingStrategy() {
        return new HpaScalingStrategy(40d, 80d);
    }

    @Bean
    public DeploymentScaler deploymentScaler(ScalingStrategy scalingStrategy, KubernetesClientService k8sClientService) {
        return new SimpleDeploymentScaler(scalingStrategy, k8sClientService);
    }
}
