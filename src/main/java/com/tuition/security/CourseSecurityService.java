package com.tuition.security;

import com.tuition.entity.Course;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.repository.CourseRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service("courseSecurityService")
@RequiredArgsConstructor
public class CourseSecurityService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    public boolean isOwner(Long courseId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) return false;

        if (user.getRole() == Role.ADMIN) return true;

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) return false;

        return course.getTeacher().getId().equals(user.getId());
    }
}
