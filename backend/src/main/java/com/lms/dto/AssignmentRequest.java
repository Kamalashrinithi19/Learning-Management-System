package com.lms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AssignmentRequest {
    @NotBlank(message = "Assignment title is required")
    private String title;

    private String description;

    private LocalDateTime deadline;

    @NotNull(message = "Max marks are required")
    @Min(value = 1, message = "Max marks must be at least 1")
    private Integer maxMarks;
}
