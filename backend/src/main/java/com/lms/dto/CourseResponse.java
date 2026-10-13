package com.lms.dto;

import com.lms.entity.CourseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class CourseResponse {
    private Long id;
    private String title;
    private String description;
    private UserResponse instructor;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private int lessonCount;
    private int enrollmentCount;
}
