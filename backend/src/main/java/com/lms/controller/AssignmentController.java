package com.lms.controller;

import com.lms.dto.AssignmentRequest;
import com.lms.dto.AssignmentResponse;
import com.lms.dto.GradeRequest;
import com.lms.dto.SubmissionRequest;
import com.lms.dto.SubmissionResponse;
import com.lms.dto.GradeResponse;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.AssignmentService;
import com.lms.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
@Tag(name = "Assignments", description = "Assignment and submission management")
@SecurityRequirement(name = "bearerAuth")
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get assignments (student: enrolled courses; instructor: all their courses)")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@AuthenticationPrincipal User user) {
        if (user.getRole() == Role.STUDENT) {
            return ResponseEntity.ok(assignmentService.getAssignmentsForStudent(user.getId()));
        }
        // Instructors don't have a global "my assignments" list by default — return empty
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get assignment by ID")
    public ResponseEntity<AssignmentResponse> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Update an assignment")
    public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable Long id,
                                                                @RequestBody AssignmentRequest request,
                                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete an assignment")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        assignmentService.deleteAssignment(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ---- Submissions ----
    @PostMapping("/{id}/submissions")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit an assignment")
    public ResponseEntity<SubmissionResponse> submit(@PathVariable Long id,
                                                      @Valid @RequestBody SubmissionRequest request,
                                                      @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(submissionService.submit(id, request, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/submissions")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Get submissions for an assignment (instructor only)")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(@PathVariable Long id,
                                                                    @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getSubmissionsForAssignment(id, user.getId()));
    }
}
