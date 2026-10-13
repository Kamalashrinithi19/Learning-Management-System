package com.lms.service;

import com.lms.dto.ProgressResponse;
import com.lms.entity.Enrollment;
import com.lms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final AssignmentRepository assignmentRepository;
    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;

    public ProgressResponse getStudentProgress(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        int totalEnrolled = enrollments.size();

        int totalLessons = 0;
        int completedLessons = 0;
        int totalAssignments = 0;

        List<ProgressResponse.CourseProgressDetail> courseDetails = enrollments.stream().map(e -> {
            Long courseId = e.getCourse().getId();
            int courseLessons = lessonRepository.countByCourseId(courseId);
            int courseCompleted = lessonProgressRepository
                    .countByStudentIdAndLesson_CourseIdAndCompletedTrue(studentId, courseId);
            double pct = courseLessons > 0
                    ? Math.round((courseCompleted * 100.0 / courseLessons) * 10) / 10.0
                    : 0.0;
            int courseAssignments = assignmentRepository.findByCourseId(courseId).size();
            int courseSubmitted = (int) submissionRepository.findByStudentId(studentId)
                    .stream()
                    .filter(s -> s.getAssignment().getCourse().getId().equals(courseId))
                    .count();

            return ProgressResponse.CourseProgressDetail.builder()
                    .courseId(courseId)
                    .courseTitle(e.getCourse().getTitle())
                    .totalLessons(courseLessons)
                    .completedLessons(courseCompleted)
                    .completionPercentage(pct)
                    .totalAssignments(courseAssignments)
                    .submittedAssignments(courseSubmitted)
                    .build();
        }).collect(Collectors.toList());

        int sumLessons = courseDetails.stream().mapToInt(ProgressResponse.CourseProgressDetail::getTotalLessons).sum();
        int sumCompleted = courseDetails.stream().mapToInt(ProgressResponse.CourseProgressDetail::getCompletedLessons).sum();
        int sumAssignments = courseDetails.stream().mapToInt(ProgressResponse.CourseProgressDetail::getTotalAssignments).sum();
        int sumSubmitted = courseDetails.stream().mapToInt(ProgressResponse.CourseProgressDetail::getSubmittedAssignments).sum();

        int gradedCount = gradeRepository.findByStudentId(studentId).size();

        double overallPct = sumLessons > 0
                ? Math.round((sumCompleted * 100.0 / sumLessons) * 10) / 10.0
                : 0.0;

        return ProgressResponse.builder()
                .totalEnrolledCourses(totalEnrolled)
                .totalLessons(sumLessons)
                .completedLessons(sumCompleted)
                .overallCompletionPercentage(overallPct)
                .totalAssignments(sumAssignments)
                .submittedAssignments(sumSubmitted)
                .gradedAssignments(gradedCount)
                .courseProgress(courseDetails)
                .build();
    }
}
