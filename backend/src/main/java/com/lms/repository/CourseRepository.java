package com.lms.repository;

import com.lms.entity.Course;
import com.lms.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findByInstructorId(Long instructorId);
    List<Course> findByStatus(CourseStatus status);

    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Course> searchCourses(@Param("q") String query);

    @Query("SELECT c FROM Course c JOIN c.enrollments e WHERE e.student.id = :studentId")
    List<Course> findEnrolledCoursesByStudentId(@Param("studentId") Long studentId);
}
