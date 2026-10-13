package com.lms.dto;

import com.lms.entity.SubmissionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class SubmissionResponse {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private UserResponse student;
    private String submissionText;
    private LocalDateTime submittedAt;
    private SubmissionStatus status;
    private GradeResponse grade; // may be null
}
