package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.audit.AdminAuditLogResponse;
import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.repository.AdminAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminAuditLogService {

  private final AdminAuditLogRepository adminAuditLogRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void record(
      Long adminUserId,
      AdminAuditLog.ActionType actionType,
      AdminAuditLog.TargetType targetType,
      Long targetId,
      String reason,
      Object beforeData,
      Object afterData
  ) {
    AdminAuditLog logEntity = AdminAuditLog.builder()
        .adminUserId(adminUserId)
        .actionType(actionType)
        .targetType(targetType)
        .targetId(targetId)
        .reason(reason)
        .beforeData(toJson(beforeData))
        .afterData(toJson(afterData))
        .build();

    adminAuditLogRepository.save(logEntity);
  }

  @Transactional(readOnly = true)
  public Page<AdminAuditLogResponse> getAuditLogs(Pageable pageable) {
    return adminAuditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
        .map(AdminAuditLogResponse::from);
  }

  private String toJson(Object value) {
    if (value == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (Exception e) {
      log.warn("Failed to serialize audit payload: {}", e.getMessage());
      return String.valueOf(value);
    }
  }
}
