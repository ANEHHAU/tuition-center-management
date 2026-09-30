package com.tuition.repository.spec;

import com.tuition.entity.User;
import org.springframework.data.jpa.domain.Specification;

/**
 * Các Specification hỗ trợ tìm kiếm cho User.
 */
public class UserSpecification {

    /**
     * Tìm kiếm 3-trong-1 theo keyword (fullName OR email OR phone).
     */
    public static Specification<User> searchKeyword(String keyword) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return null;
            }
            String likePattern = "%" + keyword.toLowerCase().trim() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), likePattern),
                    cb.like(cb.lower(root.get("email")), likePattern),
                    cb.like(cb.lower(root.get("phone")), likePattern)
            );
        };
    }
}
