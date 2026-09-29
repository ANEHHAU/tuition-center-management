package com.tuition.service;

import com.tuition.dto.CourseRequest;
import com.tuition.dto.CourseResponse;
import com.tuition.entity.Course;
import com.tuition.entity.CourseStatus;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.exception.BusinessException;
import com.tuition.repository.CourseRepository;
import com.tuition.repository.GroupRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service quản lý Khóa học (Course).
 * Teacher chỉ thấy course của mình. Admin thấy tất cả.
 */
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    /**
     * Tạo khóa học mới.
     * - TEACHER: tự động gán teacher = currentUser
     * - ADMIN: cho phép truyền teacherId để gán cho teacher khác
     */
    @Transactional
    public CourseResponse create(CourseRequest req, User currentUser) {
        User teacher = resolveTeacher(req.teacherId(), currentUser);

        // Validate: không trùng tên course trong cùng 1 teacher
        if (courseRepository.existsByNameAndTeacherId(req.name(), teacher.getId())) {
            throw new BusinessException("Giáo viên đã có khóa học trùng tên: " + req.name());
        }

        Course course = Course.builder()
                .name(req.name())
                .pricePerSession(req.pricePerSession())
                .description(req.description())
                .teacher(teacher)
                .coverUrl(req.coverUrl())
                .coverPublicId(req.coverPublicId())
                .status(req.status() != null ? req.status() : CourseStatus.ACTIVE)
                .build();

        Course saved = courseRepository.save(course);
        return CourseResponse.fromEntity(saved, 0);
    }

    /**
     * Cập nhật khóa học. Chỉ chủ sở hữu hoặc ADMIN mới có quyền.
     */
    @Transactional
    public CourseResponse update(Long id, CourseRequest req, User currentUser) {
        Course course = getCourseOrThrow(id);
        checkOwnership(course, currentUser);

        course.setName(req.name());
        course.setPricePerSession(req.pricePerSession());
        course.setDescription(req.description());
        if (req.coverUrl() != null) course.setCoverUrl(req.coverUrl());
        if (req.coverPublicId() != null) course.setCoverPublicId(req.coverPublicId());
        if (req.status() != null) course.setStatus(req.status());

        Course saved = courseRepository.save(course);
        long groupCount = groupRepository.countByCourseId(id);
        return CourseResponse.fromEntity(saved, groupCount);
    }

    /**
     * Xóa mềm (soft delete): đặt status = INACTIVE
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Course course = getCourseOrThrow(id);
        checkOwnership(course, currentUser);
        course.setStatus(CourseStatus.INACTIVE);
        courseRepository.save(course);
    }

    /**
     * Lấy chi tiết khóa học. Check quyền xem.
     */
    @Transactional(readOnly = true)
    public CourseResponse getById(Long id, User currentUser) {
        Course course = getCourseOrThrow(id);
        checkOwnership(course, currentUser);
        long groupCount = groupRepository.countByCourseId(id);
        return CourseResponse.fromEntity(course, groupCount);
    }

    /**
     * Liệt kê khóa học theo role:
     * - TEACHER: chỉ course của mình
     * - ADMIN: tất cả
     */
    @Transactional(readOnly = true)
    public List<CourseResponse> listByCurrentUser(User currentUser) {
        List<Course> courses;
        if (currentUser.getRole() == Role.ADMIN) {
            courses = courseRepository.findAll();
        } else {
            courses = courseRepository.findByTeacherId(currentUser.getId());
        }
        return courses.stream()
                .map(c -> CourseResponse.fromEntity(c, groupRepository.countByCourseId(c.getId())))
                .toList();
    }

    // ========= Helper methods =========

    /**
     * Xác định teacher cho course:
     * - TEACHER → dùng currentUser
     * - ADMIN → dùng teacherId nếu có, không thì bắt buộc truyền
     */
    private User resolveTeacher(Long teacherIdFromRequest, User currentUser) {
        if (currentUser.getRole() == Role.TEACHER) {
            return currentUser;
        }
        // ADMIN: phải chỉ định teacherId
        if (teacherIdFromRequest == null) {
            throw new BusinessException("Admin phải chỉ định teacherId khi tạo khóa học");
        }
        User teacher = userRepository.findById(teacherIdFromRequest)
                .orElseThrow(() -> new BusinessException("Không tìm thấy giáo viên với id: " + teacherIdFromRequest));
        if (teacher.getRole() != Role.TEACHER) {
            throw new BusinessException("User với id " + teacherIdFromRequest + " không phải TEACHER");
        }
        return teacher;
    }

    /**
     * Kiểm tra quyền sở hữu course
     */
    private void checkOwnership(Course course, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;
        if (!course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác trên khóa học này");
        }
    }

    public Course getCourseOrThrow(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy khóa học với id: " + id));
    }
}
