package com.example.myauth.controller.admin;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.admin.audit.AdminAuditLogResponse;
import com.example.myauth.service.admin.AdminAuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/audit-logs")
@RequiredArgsConstructor
public class AdminAuditLogController {

  private final AdminAuditLogService adminAuditLogService;

  @GetMapping
  public ResponseEntity<ApiResponse<Page<AdminAuditLogResponse>>> getAuditLogs(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    int normalizedSize = Math.max(1, Math.min(size, 100));
    Pageable pageable = PageRequest.of(Math.max(page, 0), normalizedSize, Sort.by("createdAt").descending());
    Page<AdminAuditLogResponse> response = adminAuditLogService.getAuditLogs(pageable);
    return ResponseEntity.ok(ApiResponse.success("Admin audit logs fetched.", response));
  }
}
