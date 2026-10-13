package com.lms.controller;

import com.lms.dto.EnrollmentResponse;
import com.lms.entity.User;
import com.lms.service.EnrollmentService;
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
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Student enrollment management")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get my enrolled courses")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(user.getId()));
    }

    @DeleteMapping("/{id}/drop")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Drop a course enrollment")
    public ResponseEntity<Void> dropCourse(@PathVariable Long id, @AuthenticationPrincipal User user) {
        enrollmentService.dropCourse(id, user.getId());
        return ResponseEntity.noContent().build();
    }
}
