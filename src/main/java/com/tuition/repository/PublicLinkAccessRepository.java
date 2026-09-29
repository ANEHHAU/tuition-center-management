package com.tuition.repository;

import com.tuition.entity.PublicLinkAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublicLinkAccessRepository extends JpaRepository<PublicLinkAccess, Long> {
}
