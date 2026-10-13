package com.lms.controller;

import com.lms.dto.GradeResponse;
import com.lms.entity.User;
import com.lms.service.SubmissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
@Tag(name = "Grades", description = "Grade retrieval endpoints")
@SecurityRequirement(name = "bearerAuth")
public class GradeController {

    private final SubmissionService submissionService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my grades")
    public ResponseEntity<List<GradeResponse>> getMyGrades(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(submissionService.getMyGrades(user.getId()));
    }
}
