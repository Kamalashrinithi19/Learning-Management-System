package com.lms.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data @Builder
public class ProgressResponse {
    private int totalEnrolledCourses;
    private int totalLessons;
    private int completedLessons;
    private double overallCompletionPercentage;
    private int totalAssignments;
    private int submittedAssignments;
    private int gradedAssignments;
    private List<CourseProgressDetail> courseProgress;

    @Data @Builder
    public static class CourseProgressDetail {
        private Long courseId;
        private String courseTitle;
        private int totalLessons;
        private int completedLessons;
        private double completionPercentage;
        private int totalAssignments;
        private int submittedAssignments;
    }
}
