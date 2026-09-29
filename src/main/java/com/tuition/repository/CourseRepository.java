package com.tuition.repository;

import com.tuition.entity.Course;
import com.tuition.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTeacherId(Long teacherId);

    List<Course> findByTeacherIdAndStatus(Long teacherId, CourseStatus status);

    boolean existsByNameAndTeacherId(String name, Long teacherId);
}
