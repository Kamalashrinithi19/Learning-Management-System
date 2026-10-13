package com.lms.dto;

import com.lms.entity.EnrollmentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class EnrollmentResponse {
    private Long id;
    private CourseResponse course;
    private UserResponse student;
    private EnrollmentStatus status;
    private LocalDateTime enrolledAt;
}
