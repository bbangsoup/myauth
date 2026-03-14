package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.user.AdminUserDetailResponse;
import com.example.myauth.dto.admin.user.AdminUserListItemResponse;
import com.example.myauth.dto.admin.user.AdminUserRoleUpdateRequest;
import com.example.myauth.dto.admin.user.AdminUserStatusUpdateRequest;
import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.User;
import com.example.myauth.exception.AdminAccessDeniedException;
import com.example.myauth.exception.UserNotFoundException;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

  private final UserRepository userRepository;
  private final AdminAuditLogService adminAuditLogService;

  @Transactional(readOnly = true)
  public Page<AdminUserListItemResponse> getUsers(
      Pageable pageable,
      String keyword,
      User.Status status,
      User.Role role
  ) {
    String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    return userRepository.searchForAdmin(normalizedKeyword, status, role, pageable)
        .map(AdminUserListItemResponse::from);
  }

  @Transactional(readOnly = true)
  public AdminUserDetailResponse getUserDetail(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
    return AdminUserDetailResponse.from(user);
  }

  @Transactional
  public AdminUserDetailResponse updateStatus(User adminUser, Long targetUserId, AdminUserStatusUpdateRequest request) {
    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + targetUserId));

    if (adminUser.getId().equals(targetUserId)
        && (request.getStatus() == User.Status.SUSPENDED
        || request.getStatus() == User.Status.INACTIVE
        || request.getStatus() == User.Status.DELETED)) {
      throw new IllegalArgumentException("You cannot deactivate or suspend your own account.");
    }

    Map<String, Object> before = statusSnapshot(target);
    applyStatus(target, request);
    User saved = userRepository.save(target);
    Map<String, Object> after = statusSnapshot(saved);

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.USER_STATUS_CHANGED,
        AdminAuditLog.TargetType.USER,
        targetUserId,
        request.getReason(),
        before,
        after
    );

    return AdminUserDetailResponse.from(saved);
  }

  @Transactional
  public AdminUserDetailResponse updateRole(User adminUser, Long targetUserId, AdminUserRoleUpdateRequest request) {
    if (!Boolean.TRUE.equals(adminUser.getIsSuperUser())) {
      throw new AdminAccessDeniedException("Only super admins can change user roles.");
    }

    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException("User not found: " + targetUserId));

    if (adminUser.getId().equals(targetUserId)) {
      throw new IllegalArgumentException("You cannot change your own role.");
    }

    boolean nextSuperUser = request.getIsSuperUser() != null ? request.getIsSuperUser() : target.getIsSuperUser();
    if (request.getRole() == User.Role.ROLE_USER) {
      nextSuperUser = false;
    }
    if (request.getRole() == User.Role.ROLE_USER && Boolean.TRUE.equals(request.getIsSuperUser())) {
      throw new IllegalArgumentException("ROLE_USER cannot be a super user.");
    }

    if (Boolean.TRUE.equals(target.getIsSuperUser()) && !nextSuperUser) {
      long superAdminCount = userRepository.countByIsSuperUserTrue();
      if (superAdminCount <= 1) {
        throw new IllegalArgumentException("The last super admin cannot be downgraded.");
      }
    }

    Map<String, Object> before = roleSnapshot(target);
    target.setRole(request.getRole());
    target.setIsSuperUser(nextSuperUser);
    User saved = userRepository.save(target);
    Map<String, Object> after = roleSnapshot(saved);

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.USER_ROLE_CHANGED,
        AdminAuditLog.TargetType.USER,
        targetUserId,
        request.getReason(),
        before,
        after
    );

    return AdminUserDetailResponse.from(saved);
  }

  private void applyStatus(User target, AdminUserStatusUpdateRequest request) {
    target.setStatus(request.getStatus());
    switch (request.getStatus()) {
      case ACTIVE -> {
        target.setIsActive(true);
        target.setAccountLockedUntil(null);
      }
      case SUSPENDED -> {
        target.setIsActive(false);
        if (request.getSuspendDays() != null) {
          target.setAccountLockedUntil(LocalDateTime.now().plusDays(request.getSuspendDays()));
        }
      }
      case INACTIVE, DELETED, PENDING_VERIFICATION -> target.setIsActive(false);
    }
  }

  private Map<String, Object> statusSnapshot(User user) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("status", user.getStatus());
    data.put("isActive", user.getIsActive());
    data.put("accountLockedUntil", user.getAccountLockedUntil());
    return data;
  }

  private Map<String, Object> roleSnapshot(User user) {
    Map<String, Object> data = new LinkedHashMap<>();
    data.put("role", user.getRole());
    data.put("isSuperUser", user.getIsSuperUser());
    return data;
  }
}
