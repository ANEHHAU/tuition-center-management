package com.tuition.repository;

import com.tuition.entity.Role;
import com.tuition.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    List<User> findByCreatedById(Long createdById);

    List<User> findByRoleAndCreatedById(Role role, Long createdById);
}
