package com.lms.repository;

import com.lms.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {
    Optional<Grade> findBySubmissionId(Long submissionId);

    @Query("SELECT g FROM Grade g WHERE g.submission.student.id = :studentId")
    List<Grade> findByStudentId(@Param("studentId") Long studentId);
}
