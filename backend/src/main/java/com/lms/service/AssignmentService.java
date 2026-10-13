package com.lms.service;

import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.UnauthorizedException;
import com.lms.repository.AssignmentRepository;
import com.lms.repository.EnrollmentRepository;
import com.lms.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final CourseService courseService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EnrollmentRepository enrollmentRepository;
    private final SubmissionRepository submissionRepository;

    public AssignmentResponse createAssignment(Long courseId, AssignmentRequest request, Long instructorId) {
        Course course = courseService.findById(courseId);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can create assignments");
        }
        Assignment assignment = Assignment.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .deadline(request.getDeadline())
                .maxMarks(request.getMaxMarks())
                .build();
        Assignment saved = assignmentRepository.save(assignment);

        // Notify enrolled students
        enrollmentRepository.findByCourseId(courseId).forEach(enrollment -> {
            notificationService.createNotification(
                    enrollment.getStudent(),
                    "New Assignment: " + request.getTitle(),
                    "A new assignment has been posted in " + course.getTitle(),
                    NotificationType.ASSIGNMENT);
        });

        return toResponse(saved, null, null);
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId).stream()
                .map(a -> toResponse(a, null, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AssignmentResponse> getAssignmentsForStudent(Long studentId) {
        return assignmentRepository.findAssignmentsForStudent(studentId).stream()
                .map(a -> {
                    var sub = submissionRepository.findByAssignmentIdAndStudentId(a.getId(), studentId);
                    return toResponse(a, sub.isPresent(), sub.map(Submission::getId).orElse(null));
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AssignmentResponse getAssignmentById(Long id) {
        return toResponse(findById(id), null, null);
    }

    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest request, Long instructorId) {
        Assignment assignment = findById(assignmentId);
        if (!assignment.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can update this assignment");
        }
        if (StringUtils.hasText(request.getTitle()))       assignment.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) assignment.setDescription(request.getDescription());
        if (request.getDeadline() != null)                 assignment.setDeadline(request.getDeadline());
        if (request.getMaxMarks() != null)                 assignment.setMaxMarks(request.getMaxMarks());
        return toResponse(assignmentRepository.save(assignment), null, null);
    }

    public void deleteAssignment(Long assignmentId, Long instructorId) {
        Assignment assignment = findById(assignmentId);
        if (!assignment.getCourse().getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can delete this assignment");
        }
        assignmentRepository.delete(assignment);
    }

    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Assignment", id));
    }

    public AssignmentResponse toResponse(Assignment a, Boolean submitted, Long submissionId) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .courseId(a.getCourse().getId())
                .courseTitle(a.getCourse().getTitle())
                .title(a.getTitle())
                .description(a.getDescription())
                .deadline(a.getDeadline())
                .maxMarks(a.getMaxMarks())
                .createdAt(a.getCreatedAt())
                .submitted(Boolean.TRUE.equals(submitted))
                .submissionId(submissionId)
                .build();
    }
}
