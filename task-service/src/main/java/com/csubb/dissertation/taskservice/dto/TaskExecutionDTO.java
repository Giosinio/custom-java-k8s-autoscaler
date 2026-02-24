package com.csubb.dissertation.taskservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO used to send the number of milliseconds we intend or task to run.
 */
@Data
public class TaskExecutionDTO {

    /**
     * Duration of the task in milliseconds
     */
    @NotNull
    private Long numberOfMillis;
}
