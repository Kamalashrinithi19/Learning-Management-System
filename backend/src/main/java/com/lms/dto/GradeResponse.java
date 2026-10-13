package com.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class GradeResponse {
    private Long id;
    private Long submissionId;
    private String assignmentTitle;
    private String courseTitle;
    private Double marks;
    private Integer maxMarks;
    private String feedback;
    private UserResponse gradedBy;
    private LocalDateTime gradedAt;
    private UserResponse student;
}
