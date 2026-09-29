package com.tuition.repository;

import com.tuition.entity.Group;
import com.tuition.entity.GroupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByPublicToken(String publicToken);

    Optional<Group> findByPublicTokenAndTokenEnabledTrue(String publicToken);

    List<Group> findByCourseId(Long courseId);

    List<Group> findByTeacherId(Long teacherId);

    boolean existsByNameAndCourseId(String name, Long courseId);

    long countByCourseId(Long courseId);
}
