package com.lms.service;

import com.lms.dto.GradeRequest;
import com.lms.dto.GradeResponse;
import com.lms.dto.SubmissionRequest;
import com.lms.dto.SubmissionResponse;
import com.lms.entity.*;
import com.lms.exception.BusinessException;
import com.lms.exception.DuplicateResourceException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.UnauthorizedException;
import com.lms.repository.GradeRepository;
import com.lms.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final GradeRepository gradeRepository;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final NotificationService notificationService;

    public SubmissionResponse submit(Long assignmentId, SubmissionRequest request, Long studentId) {
        Assignment assignment = assignmentService.findById(assignmentId);

        // Check deadline
        SubmissionStatus status = SubmissionStatus.SUBMITTED;
        if (assignment.getDeadline() != null && LocalDateTime.now().isAfter(assignment.getDeadline())) {
            status = SubmissionStatus.LATE;
        }

        // Check for existing submission (update instead of duplicate)
        var existing = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId);
        if (existing.isPresent()) {
            Submission sub = existing.get();
            sub.setSubmissionText(request.getSubmissionText());
            sub.setSubmittedAt(LocalDateTime.now());
            sub.setStatus(status);
            Submission saved = submissionRepository.save(sub);

            notificationService.createNotification(
                    userService.findById(studentId),
                    "Assignment Re-submitted",
                    "Your submission for '" + assignment.getTitle() + "' has been updated.",
                    NotificationType.SUCCESS);

            return toResponse(saved);
        }

        User student = userService.findById(studentId);
        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .submissionText(request.getSubmissionText())
                .submittedAt(LocalDateTime.now())
                .status(status)
                .build();
        Submission saved = submissionRepository.save(submission);

        notificationService.createNotification(student,
                "Assignment Submitted",
                "Your submission for '" + assignment.getTitle() + "' was received.",
                NotificationType.SUCCESS);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getSubmissionsForAssignment(Long assignmentId, Long instructorId) {
        Assignment assignment = assignmentService.findById(assignmentId);
        if (!assignment.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can view submissions");
        }
        return submissionRepository.findByAssignmentId(assignmentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubmissionResponse> getMySubmissions(Long studentId) {
        return submissionRepository.findByStudentId(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubmissionResponse getSubmissionById(Long submissionId, Long userId) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));
        // Students can only see their own; instructors can see their course submissions
        if (!sub.getStudent().getId().equals(userId)
                && !sub.getAssignment().getCourse().getInstructor().getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }
        return toResponse(sub);
    }

    // ---- Grading ----
    public GradeResponse gradeSubmission(Long submissionId, GradeRequest request, Long instructorId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Submission", submissionId));

        Assignment assignment = submission.getAssignment();
        if (!assignment.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can grade submissions");
        }
        if (request.getMarks() > assignment.getMaxMarks()) {
            throw new BusinessException("Marks (" + request.getMarks() + ") cannot exceed max marks ("
                    + assignment.getMaxMarks() + ")");
        }

        User instructor = userService.findById(instructorId);
        Grade grade = gradeRepository.findBySubmissionId(submissionId).orElse(new Grade());
        grade.setSubmission(submission);
        grade.setMarks(request.getMarks());
        grade.setFeedback(request.getFeedback());
        grade.setGradedBy(instructor);
        grade.setGradedAt(LocalDateTime.now());
        Grade saved = gradeRepository.save(grade);

        submission.setStatus(SubmissionStatus.GRADED);
        submissionRepository.save(submission);

        // Notify student
        notificationService.createNotification(submission.getStudent(),
                "Assignment Graded",
                "Your submission for '" + assignment.getTitle() + "' has been graded: "
                        + request.getMarks() + "/" + assignment.getMaxMarks(),
                NotificationType.GRADE);

        return toGradeResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> getMyGrades(Long studentId) {
        return gradeRepository.findByStudentId(studentId).stream()
                .map(this::toGradeResponse)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----
    public SubmissionResponse toResponse(Submission s) {
        return SubmissionResponse.builder()
                .id(s.getId())
                .assignmentId(s.getAssignment().getId())
                .assignmentTitle(s.getAssignment().getTitle())
                .student(userService.toResponse(s.getStudent()))
                .submissionText(s.getSubmissionText())
                .submittedAt(s.getSubmittedAt())
                .status(s.getStatus())
                .grade(s.getGrade() != null ? toGradeResponse(s.getGrade()) : null)
                .build();
    }

    public GradeResponse toGradeResponse(Grade g) {
        return GradeResponse.builder()
                .id(g.getId())
                .submissionId(g.getSubmission().getId())
                .assignmentTitle(g.getSubmission().getAssignment().getTitle())
                .courseTitle(g.getSubmission().getAssignment().getCourse().getTitle())
                .marks(g.getMarks())
                .maxMarks(g.getSubmission().getAssignment().getMaxMarks())
                .feedback(g.getFeedback())
                .gradedBy(userService.toResponse(g.getGradedBy()))
                .gradedAt(g.getGradedAt())
                .student(userService.toResponse(g.getSubmission().getStudent()))
                .build();
    }
}
