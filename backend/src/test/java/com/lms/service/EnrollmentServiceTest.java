package com.lms.service;

import com.lms.entity.*;
import com.lms.exception.BusinessException;
import com.lms.exception.DuplicateResourceException;
import com.lms.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EnrollmentService Unit Tests")
class EnrollmentServiceTest {

    @Mock private EnrollmentRepository enrollmentRepository;
    @Mock private CourseService courseService;
    @Mock private UserService userService;
    @Mock private NotificationService notificationService;
    @InjectMocks private EnrollmentService enrollmentService;

    private User student;
    private Course course;

    @BeforeEach
    void setUp() {
        student = User.builder().id(1L).fullName("Student").email("s@lms.com")
                .role(Role.STUDENT).active(true).build();
        course = Course.builder().id(10L).title("Java Course")
                .status(CourseStatus.PUBLISHED).instructor(
                        User.builder().id(2L).build())
                .build();
    }

    @Test
    @DisplayName("Prevent duplicate enrollment")
    void enroll_DuplicateEnrollment_ThrowsException() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(true);

        assertThatThrownBy(() -> enrollmentService.enroll(10L, 1L))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("already enrolled");
    }

    @Test
    @DisplayName("Cannot enroll in unpublished course")
    void enroll_UnpublishedCourse_ThrowsException() {
        course.setStatus(CourseStatus.DRAFT);
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(courseService.findById(10L)).thenReturn(course);

        assertThatThrownBy(() -> enrollmentService.enroll(10L, 1L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not available");
    }

    @Test
    @DisplayName("Successful enrollment sends notification")
    void enroll_Success_SendsNotification() {
        when(enrollmentRepository.existsByStudentIdAndCourseId(1L, 10L)).thenReturn(false);
        when(courseService.findById(10L)).thenReturn(course);
        when(userService.findById(1L)).thenReturn(student);
        Enrollment e = Enrollment.builder().id(1L).student(student).course(course)
                .status(EnrollmentStatus.ACTIVE).build();
        when(enrollmentRepository.save(any())).thenReturn(e);
        when(courseService.toCourseResponse(any())).thenReturn(null);
        when(userService.toResponse(any())).thenReturn(null);

        enrollmentService.enroll(10L, 1L);

        verify(notificationService).createNotification(eq(student), contains("Enrolled"), any(), eq(NotificationType.ENROLLMENT));
    }
}
