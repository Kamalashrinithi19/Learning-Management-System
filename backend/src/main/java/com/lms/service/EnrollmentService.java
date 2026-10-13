package com.lms.service;

import com.lms.dto.EnrollmentResponse;
import com.lms.entity.*;
import com.lms.exception.BusinessException;
import com.lms.exception.DuplicateResourceException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final NotificationService notificationService;

    public EnrollmentResponse enroll(Long courseId, Long studentId) {
        if (enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId)) {
            throw new DuplicateResourceException("You are already enrolled in this course");
        }
        Course course = courseService.findById(courseId);
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("This course is not available for enrollment");
        }
        User student = userService.findById(studentId);
        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build();
        Enrollment saved = enrollmentRepository.save(enrollment);

        // Notify student
        notificationService.createNotification(student,
                "Enrolled Successfully",
                "You have been enrolled in: " + course.getTitle(),
                NotificationType.ENROLLMENT);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getMyEnrollments(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getEnrollmentsForCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean isEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    public void dropCourse(Long enrollmentId, Long studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));
        if (!enrollment.getStudent().getId().equals(studentId)) {
            throw new BusinessException("You cannot drop this enrollment");
        }
        enrollment.setStatus(EnrollmentStatus.DROPPED);
        enrollmentRepository.save(enrollment);
    }

    private EnrollmentResponse toResponse(Enrollment e) {
        return EnrollmentResponse.builder()
                .id(e.getId())
                .course(courseService.toCourseResponse(e.getCourse()))
                .student(userService.toResponse(e.getStudent()))
                .status(e.getStatus())
                .enrolledAt(e.getEnrolledAt())
                .build();
    }
}
