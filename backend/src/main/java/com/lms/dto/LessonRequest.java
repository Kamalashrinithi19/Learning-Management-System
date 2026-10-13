package com.lms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LessonRequest {
    @NotBlank(message = "Lesson title is required")
    private String title;

    private String description;
    private String contentUrl;
    private String textContent;

    @NotNull(message = "Sequence number is required")
    private Integer sequenceNumber;
}
