package com.csubb.dissertation.customautoscaler;

import com.csubb.dissertation.customautoscaler.algorithm.HpaScalingStrategy;
import com.csubb.dissertation.customautoscaler.algorithm.ScalingStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

@EnableScheduling
@SpringBootApplication
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
}
