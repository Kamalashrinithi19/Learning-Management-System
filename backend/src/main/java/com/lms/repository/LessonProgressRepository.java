package com.lms.repository;

import com.lms.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByStudentIdAndLessonId(Long studentId, Long lessonId);
    List<LessonProgress> findByStudentId(Long studentId);

    int countByStudentIdAndCompletedTrue(Long studentId);

    int countByStudentIdAndLesson_CourseIdAndCompletedTrue(Long studentId, Long courseId);
}
