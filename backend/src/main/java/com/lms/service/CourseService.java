package com.lms.service;

import com.lms.dto.CourseRequest;
import com.lms.dto.CourseResponse;
import com.lms.dto.LessonRequest;
import com.lms.dto.LessonResponse;
import com.lms.entity.*;
import com.lms.exception.ResourceNotFoundException;
import com.lms.exception.UnauthorizedException;
import com.lms.repository.CourseRepository;
import com.lms.repository.LessonRepository;
import com.lms.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;

    public CourseResponse createCourse(CourseRequest request, Long instructorId) {
        User instructor = userService.findById(instructorId);
        CourseStatus status = CourseStatus.DRAFT;
        if (StringUtils.hasText(request.getStatus())) {
            try { status = CourseStatus.valueOf(request.getStatus()); } catch (Exception ignored) {}
        }
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .instructor(instructor)
                .status(status)
                .build();
        return toCourseResponse(courseRepository.save(course));
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        return courseRepository.findAll().stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED).stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByInstructor(Long instructorId) {
        return courseRepository.findByInstructorId(instructorId).stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {
        return toCourseResponse(findById(id));
    }

    public CourseResponse updateCourse(Long id, CourseRequest request, Long instructorId) {
        Course course = findById(id);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("You are not authorized to update this course");
        }
        if (StringUtils.hasText(request.getTitle()))       course.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) course.setDescription(request.getDescription());
        if (StringUtils.hasText(request.getStatus())) {
            try { course.setStatus(CourseStatus.valueOf(request.getStatus())); } catch (Exception ignored) {}
        }
        return toCourseResponse(courseRepository.save(course));
    }

    public void deleteCourse(Long id, Long instructorId) {
        Course course = findById(id);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("You are not authorized to delete this course");
        }
        courseRepository.delete(course);
    }

    // ---- Lessons ----
    public LessonResponse addLesson(Long courseId, LessonRequest request, Long instructorId) {
        Course course = findById(courseId);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can add lessons");
        }
        Lesson lesson = Lesson.builder()
                .course(course)
                .title(request.getTitle())
                .description(request.getDescription())
                .contentUrl(request.getContentUrl())
                .textContent(request.getTextContent())
                .sequenceNumber(request.getSequenceNumber())
                .build();
        return toLessonResponse(lessonRepository.save(lesson), null);
    }

    public LessonResponse updateLesson(Long courseId, Long lessonId, LessonRequest request, Long instructorId) {
        Course course = findById(courseId);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can update lessons");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        if (!lesson.getCourse().getId().equals(courseId)) {
            throw new ResourceNotFoundException("Lesson not found in this course");
        }
        if (StringUtils.hasText(request.getTitle()))       lesson.setTitle(request.getTitle());
        if (StringUtils.hasText(request.getDescription())) lesson.setDescription(request.getDescription());
        if (request.getContentUrl() != null)               lesson.setContentUrl(request.getContentUrl());
        if (request.getTextContent() != null)              lesson.setTextContent(request.getTextContent());
        if (request.getSequenceNumber() != null)           lesson.setSequenceNumber(request.getSequenceNumber());
        return toLessonResponse(lessonRepository.save(lesson), null);
    }

    public void deleteLesson(Long courseId, Long lessonId, Long instructorId) {
        Course course = findById(courseId);
        if (!course.getInstructor().getId().equals(instructorId)) {
            throw new UnauthorizedException("Only the course instructor can delete lessons");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson", lessonId));
        lessonRepository.delete(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsForCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderBySequenceNumberAsc(courseId).stream()
                .map(l -> toLessonResponse(l, null))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> searchCourses(String query) {
        return courseRepository.searchCourses(query).stream()
                .map(this::toCourseResponse)
                .collect(Collectors.toList());
    }

    // ---- Helpers ----
    public Course findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));
    }

    public CourseResponse toCourseResponse(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructor(userService.toResponse(course.getInstructor()))
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .lessonCount(course.getLessons().size())
                .enrollmentCount(course.getEnrollments().size())
                .build();
    }

    public LessonResponse toLessonResponse(Lesson lesson, Boolean completed) {
        return LessonResponse.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .courseTitle(lesson.getCourse().getTitle())
                .title(lesson.getTitle())
                .description(lesson.getDescription())
                .contentUrl(lesson.getContentUrl())
                .textContent(lesson.getTextContent())
                .sequenceNumber(lesson.getSequenceNumber())
                .createdAt(lesson.getCreatedAt())
                .completed(completed)
                .build();
    }
}
