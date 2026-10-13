package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourseRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200)
    private String title;

    private String description;

    private String status; // DRAFT, PUBLISHED, ARCHIVED
}
