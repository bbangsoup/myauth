package com.example.myauth.dto.admin.audit;

import com.example.myauth.entity.AdminAuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class AdminAuditLogResponse {
  private Long id;
  private Long adminUserId;
  private AdminAuditLog.ActionType actionType;
  private AdminAuditLog.TargetType targetType;
  private Long targetId;
  private String reason;
  private String beforeData;
  private String afterData;
  private LocalDateTime createdAt;

  public static AdminAuditLogResponse from(AdminAuditLog log) {
    return AdminAuditLogResponse.builder()
        .id(log.getId())
        .adminUserId(log.getAdminUserId())
        .actionType(log.getActionType())
        .targetType(log.getTargetType())
        .targetId(log.getTargetId())
        .reason(log.getReason())
        .beforeData(log.getBeforeData())
        .afterData(log.getAfterData())
        .createdAt(log.getCreatedAt())
        .build();
  }
}
