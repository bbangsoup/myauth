package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.post.AdminDeleteRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.admin.AdminModerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminModerationController {

  private final AdminModerationService adminModerationService;

  @DeleteMapping("/posts/{postId}")
  public ResponseEntity<ApiResponse<Void>> forceDeletePost(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long postId,
      @Valid @RequestBody(required = false) AdminDeleteRequest request
  ) {
    String reason = request != null ? request.getReason() : null;
    adminModerationService.forceDeletePost(adminUser, postId, reason);
    return ResponseEntity.ok(ApiResponse.success("Post force deleted.", null));
  }

  @DeleteMapping("/comments/{commentId}")
  public ResponseEntity<ApiResponse<Void>> forceDeleteComment(
      @AuthenticationPrincipal User adminUser,
      @PathVariable Long commentId,
      @Valid @RequestBody(required = false) AdminDeleteRequest request
  ) {
    String reason = request != null ? request.getReason() : null;
    adminModerationService.forceDeleteComment(adminUser, commentId, reason);
    return ResponseEntity.ok(ApiResponse.success("Comment force deleted.", null));
  }
}
