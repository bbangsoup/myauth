package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.user.AdminUserDetailResponse;
import com.example.myauth.dto.admin.user.AdminUserListItemResponse;
import com.example.myauth.dto.admin.user.AdminUserRoleUpdateRequest;
import com.example.myauth.dto.admin.user.AdminUserStatusUpdateRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.admin.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

  private final AdminUserService adminUserService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminUserListItemResponse>>> getUsers(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) User.Status status,
      @RequestParam(required = false) User.Role role
  ) {
    int normalizedSize = Math.max(1, Math.min(size, 100));
    Pageable pageable = PageRequest.of(Math.max(page, 0), normalizedSize, Sort.by("createdAt").descending());

    Page<AdminUserListItemResponse> response = adminUserService.getUsers(pageable, keyword, status, role);
    return ResponseEntity.ok(ApiResponse.success("Admin user list fetched.", response));
  }

  @GetMapping("/{userId}")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> getUserDetail(@PathVariable Long userId) {
    return ResponseEntity.ok(ApiResponse.success("Admin user detail fetched.", adminUserService.getUserDetail(userId)));
  }

  @PatchMapping("/{userId}/status")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateStatus(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserStatusUpdateRequest request
  ) {
    AdminUserDetailResponse updated = adminUserService.updateStatus(adminUser, userId, request);
    return ResponseEntity.ok(ApiResponse.success("User status updated.", updated));
  }

  @PatchMapping("/{userId}/role")
  public ResponseEntity<ApiResponse<AdminUserDetailResponse>> updateRole(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long userId,
      @Valid @RequestBody AdminUserRoleUpdateRequest request
  ) {
    AdminUserDetailResponse updated = adminUserService.updateRole(adminUser, userId, request);
    return ResponseEntity.ok(ApiResponse.success("User role updated.", updated));
  }
}
