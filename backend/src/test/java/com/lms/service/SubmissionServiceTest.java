package com.lms.service;

import com.lms.dto.GradeRequest;
import com.lms.dto.SubmissionRequest;
import com.lms.entity.*;
import com.lms.exception.BusinessException;
import com.lms.repository.GradeRepository;
import com.lms.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SubmissionService Unit Tests")
class SubmissionServiceTest {

    @Mock private SubmissionRepository submissionRepository;
    @Mock private GradeRepository gradeRepository;
    @Mock private AssignmentService assignmentService;
    @Mock private UserService userService;
    @Mock private NotificationService notificationService;
    @InjectMocks private SubmissionService submissionService;

    private User student;
    private User instructor;
    private Assignment assignment;
    private Course course;

    @BeforeEach
    void setUp() {
        instructor = User.builder().id(2L).fullName("Instructor").email("i@lms.com")
                .role(Role.INSTRUCTOR).active(true).build();
        course = Course.builder().id(1L).title("Java Course").instructor(instructor).build();
        student = User.builder().id(1L).fullName("Student").email("s@lms.com")
                .role(Role.STUDENT).active(true).build();
        assignment = Assignment.builder().id(5L).title("HW1").maxMarks(100)
                .deadline(LocalDateTime.now().plusDays(7)).course(course).build();
    }

    @Test
    @DisplayName("Submit assignment creates submission successfully")
    void submit_Success() {
        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("My answer");

        when(assignmentService.findById(5L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 1L)).thenReturn(Optional.empty());
        when(userService.findById(1L)).thenReturn(student);
        Submission saved = Submission.builder().id(1L).assignment(assignment).student(student)
                .submissionText("My answer").submittedAt(LocalDateTime.now())
                .status(SubmissionStatus.SUBMITTED).build();
        when(submissionRepository.save(any())).thenReturn(saved);
        when(userService.toResponse(any())).thenReturn(null);

        var response = submissionService.submit(5L, req, 1L);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(SubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Submission after deadline is marked LATE")
    void submit_AfterDeadline_MarkedLate() {
        assignment.setDeadline(LocalDateTime.now().minusDays(1));
        SubmissionRequest req = new SubmissionRequest();
        req.setSubmissionText("Late answer");

        when(assignmentService.findById(5L)).thenReturn(assignment);
        when(submissionRepository.findByAssignmentIdAndStudentId(5L, 1L)).thenReturn(Optional.empty());
        when(userService.findById(1L)).thenReturn(student);
        when(submissionRepository.save(any())).thenAnswer(inv -> {
            Submission s = inv.getArgument(0);
            assertThat(s.getStatus()).isEqualTo(SubmissionStatus.LATE);
            return s;
        });
        when(userService.toResponse(any())).thenReturn(null);

        submissionService.submit(5L, req, 1L);
    }

    @Test
    @DisplayName("Grade marks exceeding max marks throws exception")
    void grade_ExceedingMaxMarks_ThrowsException() {
        GradeRequest gradeReq = new GradeRequest();
        gradeReq.setMarks(110.0); // more than maxMarks=100

        Submission submission = Submission.builder().id(1L).assignment(assignment)
                .student(student).submittedAt(LocalDateTime.now()).status(SubmissionStatus.SUBMITTED).build();
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));

        assertThatThrownBy(() -> submissionService.gradeSubmission(1L, gradeReq, 2L))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("exceed max marks");
    }

    @Test
    @DisplayName("Grade saves and notifies student")
    void grade_Success_NotifiesStudent() {
        GradeRequest gradeReq = new GradeRequest();
        gradeReq.setMarks(85.0);
        gradeReq.setFeedback("Good work!");

        Submission submission = Submission.builder().id(1L).assignment(assignment)
                .student(student).submittedAt(LocalDateTime.now()).status(SubmissionStatus.SUBMITTED).build();
        when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
        when(gradeRepository.findBySubmissionId(1L)).thenReturn(Optional.empty());
        when(userService.findById(2L)).thenReturn(instructor);
        Grade grade = Grade.builder().id(1L).submission(submission).marks(85.0)
                .feedback("Good work!").gradedBy(instructor).gradedAt(LocalDateTime.now()).build();
        when(gradeRepository.save(any())).thenReturn(grade);
        when(submissionRepository.save(any())).thenReturn(submission);
        when(userService.toResponse(any())).thenReturn(null);

        var response = submissionService.gradeSubmission(1L, gradeReq, 2L);

        assertThat(response.getMarks()).isEqualTo(85.0);
        verify(notificationService).createNotification(eq(student), contains("Graded"), any(), eq(NotificationType.GRADE));
    }
}
