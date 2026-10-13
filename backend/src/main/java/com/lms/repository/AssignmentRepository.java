package com.lms.repository;

import com.lms.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);

    @Query("SELECT a FROM Assignment a JOIN a.course c JOIN c.enrollments e " +
           "WHERE e.student.id = :studentId AND e.status = 'ACTIVE'")
    List<Assignment> findAssignmentsForStudent(@Param("studentId") Long studentId);

    @Query("SELECT a FROM Assignment a WHERE a.deadline BETWEEN :now AND :soon")
    List<Assignment> findUpcomingDeadlines(@Param("now") LocalDateTime now,
                                           @Param("soon") LocalDateTime soon);
}
