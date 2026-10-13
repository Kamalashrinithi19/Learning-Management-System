package com.lms.controller;

import com.lms.dto.GradeRequest;
import com.lms.dto.GradeResponse;
import com.lms.dto.SubmissionResponse;
import com.lms.entity.User;
import com.lms.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@Tag(name = "Submissions", description = "Submission and grading endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SubmissionController {

    private final SubmissionService submissionService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my submissions")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getMySubmissions(user.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a submission by ID")
    public ResponseEntity<SubmissionResponse> getSubmission(@PathVariable Long id,
                                                             @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getSubmissionById(id, user.getId()));
    }

    @PostMapping("/{id}/grade")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Grade a submission")
    public ResponseEntity<GradeResponse> grade(@PathVariable Long id,
                                                @Valid @RequestBody GradeRequest request,
                                                @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.gradeSubmission(id, request, user.getId()));
    }
}
