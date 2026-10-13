package com.lms.service;

import com.lms.dto.LessonResponse;
import com.lms.entity.Lesson;
import com.lms.entity.LessonProgress;
import com.lms.entity.User;
import com.lms.repository.LessonProgressRepository;
import com.lms.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class LessonProgressService {

    private final LessonProgressRepository lessonProgressRepository;
    private final LessonRepository lessonRepository;
    private final UserService userService;
    private final CourseService courseService;

    public void markLessonComplete(Long lessonId, Long studentId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found: " + lessonId));
        User student = userService.findById(studentId);

        LessonProgress progress = lessonProgressRepository
                .findByStudentIdAndLessonId(studentId, lessonId)
                .orElse(LessonProgress.builder().student(student).lesson(lesson).build());

        progress.setCompleted(true);
        progress.setCompletedAt(LocalDateTime.now());
        lessonProgressRepository.save(progress);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsWithProgress(Long courseId, Long studentId) {
        return lessonRepository.findByCourseIdOrderBySequenceNumberAsc(courseId).stream()
                .map(lesson -> {
                    boolean completed = lessonProgressRepository
                            .findByStudentIdAndLessonId(studentId, lesson.getId())
                            .map(LessonProgress::isCompleted)
                            .orElse(false);
                    return courseService.toLessonResponse(lesson, completed);
                })
                .collect(Collectors.toList());
    }
}
