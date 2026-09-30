package com.tuition.service;

import com.tuition.aspect.Auditable;
import com.tuition.dto.BaseSearchRequest;
import com.tuition.dto.PageResponse;
import com.tuition.dto.RegisterRequest;
import com.tuition.dto.UpdateUserRequest;
import com.tuition.dto.UserResponse;
import com.tuition.entity.Role;
import com.tuition.entity.User;
import com.tuition.entity.UserStatus;
import com.tuition.exception.BusinessException;
import com.tuition.repository.UserRepository;
import com.tuition.repository.spec.UserSpecification;
import com.tuition.util.SortUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    /**
     * Lấy danh sách người dùng, hỗ trợ Search 3-in-1, Filter, Pagination, và Sắp xếp tiếng Việt
     */
    public PageResponse<UserResponse> searchUsers(BaseSearchRequest req, Role role, UserStatus status) {
        Specification<User> spec = Specification.where(UserSpecification.searchKeyword(req.getKeyword()));

        if (role != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }

        // Lấy tất cả theo spec, sau đó áp dụng SortUtils nếu sort theo "name"
        if ("name".equalsIgnoreCase(req.getSortBy())) {
            List<User> users = userRepository.findAll(spec);
            List<User> sortedUsers = SortUtils.sortListByLastName(users, User::getFullName, "asc".equalsIgnoreCase(req.getSortDir()));
            
            int start = req.getPage() * req.getSize();
            int end = Math.min(start + req.getSize(), sortedUsers.size());
            List<User> pageContent = start <= end && start < sortedUsers.size() ? sortedUsers.subList(start, end) : List.of();
            
            return PageResponse.of(
                    pageContent.stream().map(UserResponse::fromEntity).toList(),
                    req.getPage(),
                    req.getSize(),
                    sortedUsers.size(),
                    (int) Math.ceil((double) sortedUsers.size() / req.getSize()),
                    req.getPage() == 0,
                    end >= sortedUsers.size()
            );
        } else {
            // Sắp xếp qua DB (VD: createdAt, email, username...)
            Sort.Direction dir = "asc".equalsIgnoreCase(req.getSortDir()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            String sortBy = req.getSortBy() != null && !req.getSortBy().isEmpty() ? req.getSortBy() : "id";
            Pageable pageable = PageRequest.of(req.getPage(), req.getSize(), Sort.by(dir, sortBy));
            
            Page<User> page = userRepository.findAll(spec, pageable);
            return PageResponse.of(page.map(UserResponse::fromEntity));
        }
    }

    public UserResponse getUserById(Long id) {
        return UserResponse.fromEntity(userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng")));
    }

    @Transactional
    @Auditable(action = "CREATE", entityType = "USER")
    public UserResponse createUser(RegisterRequest req) {
        return authService.createByAdmin(req);
    }

    @Transactional
    @Auditable(action = "UPDATE", entityType = "USER")
    public UserResponse updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (req.email() != null && !req.email().isBlank() && !req.email().equalsIgnoreCase(user.getEmail())) {
            if (userRepository.existsByEmail(req.email())) {
                throw new BusinessException("Email đã được sử dụng");
            }
            user.setEmail(req.email());
        }

        if (req.fullName() != null) user.setFullName(req.fullName());
        if (req.phone() != null) user.setPhone(req.phone());
        if (req.address() != null) user.setAddress(req.address());
        if (req.note() != null) user.setNote(req.note());
        if (req.avatarUrl() != null) user.setAvatarUrl(req.avatarUrl());
        
        // Update UserRequest from user is not having role? Let's add role to it or just don't update role for now if not present.
        return UserResponse.fromEntity(userRepository.save(user));
    }
    
    // Phương thức có thêm tham số Role (có thể dùng UpdateUserRequest mới chứa role)
    @Transactional
    @Auditable(action = "UPDATE_ROLE", entityType = "USER")
    public UserResponse updateRole(Long id, Role role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    @Auditable(action = "DELETE", entityType = "USER")
    public void softDelete(Long id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (user.getUsername().equals(currentUsername)) {
            throw new BusinessException("Bạn không thể tự xóa chính mình");
        }
        
        if (user.getRole() == Role.ADMIN) {
            long adminCount = userRepository.findByRole(Role.ADMIN).stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
            if (adminCount <= 1) {
                throw new BusinessException("Không thể xóa Admin cuối cùng của hệ thống");
            }
        }

        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Transactional
    @Auditable(action = "UPDATE_STATUS", entityType = "USER")
    public UserResponse changeStatus(Long id, UserStatus status, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));

        if (user.getUsername().equals(currentUsername) && status == UserStatus.INACTIVE) {
            throw new BusinessException("Bạn không thể tự khóa tài khoản của mình");
        }

        if (user.getRole() == Role.ADMIN && status == UserStatus.INACTIVE) {
            long adminCount = userRepository.findByRole(Role.ADMIN).stream()
                    .filter(u -> u.getStatus() == UserStatus.ACTIVE).count();
            if (adminCount <= 1) {
                throw new BusinessException("Không thể khóa Admin cuối cùng của hệ thống");
            }
        }

        user.setStatus(status);
        return UserResponse.fromEntity(userRepository.save(user));
    }

    @Transactional
    @Auditable(action = "RESET_PASSWORD", entityType = "USER")
    public void resetPassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Không tìm thấy người dùng"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
