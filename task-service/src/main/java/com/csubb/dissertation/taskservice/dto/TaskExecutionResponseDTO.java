package com.csubb.dissertation.taskservice.dto;


import java.time.LocalDateTime;

public record TaskExecutionResponseDTO(
        LocalDateTime executionStartTime,
        LocalDateTime executionEndTime
) {

}
