package com.csubb.dissertation.taskservice.controller;

import com.csubb.dissertation.taskservice.dto.TaskExecutionDTO;
import com.csubb.dissertation.taskservice.dto.TaskExecutionResponseDTO;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * This controller exposes an endpoint which simulates the execution of a task by sleeping for the specified number of milliseconds.
 */
@Slf4j
@RestController
@RequestMapping("/api/task-service")
public class TaskExecutionController {

    @PostMapping("/execute-task")
    public TaskExecutionResponseDTO executeTask(@Valid @RequestBody TaskExecutionDTO taskExecutionDTO) {
        log.info("Received task of {} millis", taskExecutionDTO.getNumberOfMillis());
        return simulateTaskExecution(taskExecutionDTO.getNumberOfMillis());
    }

    private static TaskExecutionResponseDTO simulateTaskExecution(Long numberOfMillis) {
        LocalDateTime executionStartTime = LocalDateTime.now();
        while (true) {
            LocalDateTime currentTime = LocalDateTime.now();
            if (currentTime.isAfter(executionStartTime.plusNanos(numberOfMillis * 1_000_000))) {
                return new TaskExecutionResponseDTO(executionStartTime, currentTime);
            }
        }
    }
}
