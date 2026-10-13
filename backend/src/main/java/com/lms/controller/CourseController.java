package com.lms.controller;

import com.lms.dto.*;
import com.lms.entity.Role;
import com.lms.entity.User;
import com.lms.service.AssignmentService;
import com.lms.service.CourseService;
import com.lms.service.EnrollmentService;
import com.lms.service.LessonProgressService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {

    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final EnrollmentService enrollmentService;
    private final LessonProgressService lessonProgressService;

    @PostMapping
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create a new course")
    public ResponseEntity<CourseResponse> createCourse(@Valid @RequestBody CourseRequest request,
                                                        @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(courseService.createCourse(request, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all courses (public for published)")
    public ResponseEntity<List<CourseResponse>> getCourses(
            @RequestParam(required = false) String search,
            @AuthenticationPrincipal User user) {
        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(courseService.searchCourses(search));
        }
        // Instructors see all their courses; students/anonymous see published
        if (user != null && user.getRole() == Role.INSTRUCTOR) {
            return ResponseEntity.ok(courseService.getCoursesByInstructor(user.getId()));
        }
        return ResponseEntity.ok(courseService.getPublishedCourses());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('INSTRUCTOR') or hasRole('ADMIN')")
    @Operation(summary = "Get all courses (admin/instructor)")
    public ResponseEntity<List<CourseResponse>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID")
    public ResponseEntity<CourseResponse> getCourse(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Update a course")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable Long id,
                                                        @RequestBody CourseRequest request,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(courseService.updateCourse(id, request, user.getId()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete a course")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id,
                                              @AuthenticationPrincipal User user) {
        courseService.deleteCourse(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    // ---- Enrollment ----
    @PostMapping("/{id}/enroll")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Enroll in a course")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long id,
                                                      @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(enrollmentService.enroll(id, user.getId()), HttpStatus.CREATED);
    }

    // ---- Lessons ----
    @PostMapping("/{id}/lessons")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Add a lesson to a course")
    public ResponseEntity<LessonResponse> addLesson(@PathVariable Long id,
                                                     @Valid @RequestBody LessonRequest request,
                                                     @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(courseService.addLesson(id, request, user.getId()), HttpStatus.CREATED);
    }

    @PutMapping("/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Update a lesson")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Long courseId,
                                                        @PathVariable Long lessonId,
                                                        @RequestBody LessonRequest request,
                                                        @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(courseService.updateLesson(courseId, lessonId, request, user.getId()));
    }

    @DeleteMapping("/{courseId}/lessons/{lessonId}")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Delete a lesson")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long courseId,
                                              @PathVariable Long lessonId,
                                              @AuthenticationPrincipal User user) {
        courseService.deleteLesson(courseId, lessonId, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/lessons")
    @Operation(summary = "Get lessons for a course")
    public ResponseEntity<List<LessonResponse>> getLessons(@PathVariable Long id,
                                                            @AuthenticationPrincipal User user) {
        if (user != null && user.getRole() == Role.STUDENT) {
            return ResponseEntity.ok(lessonProgressService.getLessonsWithProgress(id, user.getId()));
        }
        return ResponseEntity.ok(courseService.getLessonsForCourse(id));
    }

    @PostMapping("/{courseId}/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark a lesson as completed")
    public ResponseEntity<Void> markLessonComplete(@PathVariable Long courseId,
                                                    @PathVariable Long lessonId,
                                                    @AuthenticationPrincipal User user) {
        lessonProgressService.markLessonComplete(lessonId, user.getId());
        return ResponseEntity.ok().build();
    }

    // ---- Assignments ----
    @PostMapping("/{id}/assignments")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Create an assignment for a course")
    public ResponseEntity<AssignmentResponse> createAssignment(@PathVariable Long id,
                                                                @Valid @RequestBody AssignmentRequest request,
                                                                @AuthenticationPrincipal User user) {
        return new ResponseEntity<>(assignmentService.createAssignment(id, request, user.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/{id}/assignments")
    @Operation(summary = "Get assignments for a course")
    public ResponseEntity<List<AssignmentResponse>> getCourseAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentsForCourse(id));
    }

    // ---- Enrollment list (instructor only) ----
    @GetMapping("/{id}/enrollments")
    @PreAuthorize("hasRole('INSTRUCTOR')")
    @Operation(summary = "Get enrollments for a course")
    public ResponseEntity<List<EnrollmentResponse>> getCourseEnrollments(@PathVariable Long id) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsForCourse(id));
    }
}
