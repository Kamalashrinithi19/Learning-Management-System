package com.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class AssignmentResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private LocalDateTime deadline;
    private Integer maxMarks;
    private LocalDateTime createdAt;
    private boolean submitted;     // for student context
    private Long submissionId;     // for student context
}
