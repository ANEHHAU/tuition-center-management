package com.tuition.service;

import com.tuition.dto.EnrollmentResponse;
import com.tuition.dto.GroupRequest;
import com.tuition.dto.GroupResponse;
import com.tuition.entity.*;
import com.tuition.exception.BusinessException;
import com.tuition.repository.EnrollmentRepository;
import com.tuition.repository.GroupRepository;
import com.tuition.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service quản lý Nhóm/Lớp học (Group).
 * Bao gồm: CRUD group, thêm/xóa học sinh, quản lý public link.
 */
@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final CourseService courseService;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final PublicLinkService publicLinkService;

    /**
     * Tạo group mới. Validate course thuộc về teacher hiện tại.
     */
    @Transactional
    public GroupResponse create(GroupRequest req, User currentUser) {
        Course course = courseService.getCourseOrThrow(req.courseId());

        // Check quyền: course phải thuộc teacher hiện tại (hoặc currentUser là ADMIN)
        if (currentUser.getRole() != Role.ADMIN
                && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền tạo nhóm trong khóa học này");
        }

        // Check trùng tên group trong cùng 1 course
        if (groupRepository.existsByNameAndCourseId(req.name(), req.courseId())) {
            throw new BusinessException("Tên nhóm đã tồn tại trong khóa học này");
        }

        User teacher = course.getTeacher();

        Group group = Group.builder()
                .name(req.name())
                .course(course)
                .teacher(teacher)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(req.status() != null ? req.status() : GroupStatus.ACTIVE)
                .build();

        Group saved = groupRepository.save(group);
        return GroupResponse.fromEntity(saved, 0);
    }

    /**
     * Cập nhật group. Check quyền sở hữu.
     */
    @Transactional
    public GroupResponse update(Long id, GroupRequest req, User currentUser) {
        Group group = getGroupOrThrow(id);
        checkGroupOwnership(group, currentUser);

        group.setName(req.name());
        group.setStartDate(req.startDate());
        group.setEndDate(req.endDate());
        if (req.status() != null) group.setStatus(req.status());

        Group saved = groupRepository.save(group);
        long studentCount = enrollmentRepository.countActiveByGroupId(id);
        return GroupResponse.fromEntity(saved, studentCount);
    }

    /**
     * Xóa mềm group: đặt status = INACTIVE
     */
    @Transactional
    public void delete(Long id, User currentUser) {
        Group group = getGroupOrThrow(id);
        checkGroupOwnership(group, currentUser);
        group.setStatus(GroupStatus.INACTIVE);
        groupRepository.save(group);
    }

    /**
     * Lấy chi tiết group
     */
    @Transactional(readOnly = true)
    public GroupResponse getById(Long id, User currentUser) {
        Group group = getGroupOrThrow(id);
        checkGroupOwnership(group, currentUser);
        long studentCount = enrollmentRepository.countActiveByGroupId(id);
        return GroupResponse.fromEntity(group, studentCount);
    }

    /**
     * Liệt kê group theo course (check quyền course)
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> listByCourse(Long courseId, User currentUser) {
        Course course = courseService.getCourseOrThrow(courseId);
        if (currentUser.getRole() != Role.ADMIN
                && !course.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền xem nhóm trong khóa học này");
        }
        return groupRepository.findByCourseId(courseId).stream()
                .map(g -> GroupResponse.fromEntity(g, enrollmentRepository.countActiveByGroupId(g.getId())))
                .toList();
    }

    /**
     * Liệt kê tất cả group của teacher hiện tại
     */
    @Transactional(readOnly = true)
    public List<GroupResponse> listByCurrentTeacher(User currentUser) {
        List<Group> groups;
        if (currentUser.getRole() == Role.ADMIN) {
            groups = groupRepository.findAll();
        } else {
            groups = groupRepository.findByTeacherId(currentUser.getId());
        }
        return groups.stream()
                .map(g -> GroupResponse.fromEntity(g, enrollmentRepository.countActiveByGroupId(g.getId())))
                .toList();
    }

    // ========= Quản lý học sinh trong nhóm =========

    /**
     * Thêm học sinh vào group.
     * - Validate: user phải có role STUDENT
     * - Validate: không trùng enrollment ACTIVE trong cùng group
     */
    @Transactional
    public EnrollmentResponse addStudent(Long groupId, Long studentId, LocalDate joinDate, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new BusinessException("Không tìm thấy học sinh với id: " + studentId));

        if (student.getRole() != Role.STUDENT) {
            throw new BusinessException("User với id " + studentId + " không phải STUDENT");
        }

        // Check trùng enrollment ACTIVE trong cùng group
        enrollmentRepository.findByStudentIdAndGroupIdAndStatus(studentId, groupId, EnrollmentStatus.ACTIVE)
                .ifPresent(e -> {
                    throw new BusinessException("Học sinh đã được ghi danh ACTIVE trong nhóm này");
                });

        LocalDate effectiveJoinDate = joinDate != null ? joinDate : LocalDate.now();

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .group(group)
                .joinDate(effectiveJoinDate)
                .status(EnrollmentStatus.ACTIVE)
                .build();

        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(saved);
    }

    /**
     * Xóa học sinh khỏi group: set leave_date và status = LEFT
     */
    @Transactional
    public EnrollmentResponse removeStudent(Long groupId, Long studentId, LocalDate leaveDate, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);

        Enrollment enrollment = enrollmentRepository
                .findByStudentIdAndGroupIdAndStatus(studentId, groupId, EnrollmentStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException("Không tìm thấy enrollment ACTIVE của học sinh trong nhóm này"));

        enrollment.setLeaveDate(leaveDate != null ? leaveDate : LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.LEFT);

        Enrollment saved = enrollmentRepository.save(enrollment);
        return EnrollmentResponse.fromEntity(saved);
    }

    /**
     * Lấy danh sách học sinh ACTIVE trong group
     */
    @Transactional(readOnly = true)
    public List<EnrollmentResponse> getStudentsInGroup(Long groupId, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);
        return enrollmentRepository.findByGroupIdAndStatus(groupId, EnrollmentStatus.ACTIVE).stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
    }

    // ========= Quản lý Public Link =========

    @Transactional
    public String regeneratePublicLink(Long groupId, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);
        return publicLinkService.regenerateToken(group);
    }

    @Transactional
    public void revokePublicLink(Long groupId, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);
        publicLinkService.revokeToken(group);
    }

    @Transactional(readOnly = true)
    public String getPublicLink(Long groupId, User currentUser) {
        Group group = getGroupOrThrow(groupId);
        checkGroupOwnership(group, currentUser);
        if (group.getPublicToken() == null) {
            throw new BusinessException("Nhóm chưa có public link. Hãy tạo mới.");
        }
        return "/public/schedule/" + group.getPublicToken();
    }

    // ========= Helper methods =========

    public Group getGroupOrThrow(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy nhóm với id: " + id));
    }

    private void checkGroupOwnership(Group group, User currentUser) {
        if (currentUser.getRole() == Role.ADMIN) return;
        if (!group.getTeacher().getId().equals(currentUser.getId())) {
            throw new BusinessException("Bạn không có quyền thao tác trên nhóm này");
        }
    }
}
