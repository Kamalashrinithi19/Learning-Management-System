package com.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class LessonResponse {
    private Long id;
    private Long courseId;
    private String courseTitle;
    private String title;
    private String description;
    private String contentUrl;
    private String textContent;
    private Integer sequenceNumber;
    private LocalDateTime createdAt;
    private Boolean completed; // null if not a student context
}
