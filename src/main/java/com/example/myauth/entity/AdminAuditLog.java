package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "admin_audit_logs", indexes = {
    @Index(name = "idx_admin_audit_admin_user_id", columnList = "admin_user_id"),
    @Index(name = "idx_admin_audit_target", columnList = "target_type, target_id"),
    @Index(name = "idx_admin_audit_created_at", columnList = "created_at DESC")
})
public class AdminAuditLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "admin_user_id", nullable = false)
  private Long adminUserId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action_type", nullable = false, length = 50)
  private ActionType actionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "target_type", nullable = false, length = 30)
  private TargetType targetType;

  @Column(name = "target_id")
  private Long targetId;

  @Column(length = 500)
  private String reason;

  @Lob
  @Column(name = "before_data", columnDefinition = "TEXT")
  private String beforeData;

  @Lob
  @Column(name = "after_data", columnDefinition = "TEXT")
  private String afterData;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public enum ActionType {
    USER_STATUS_CHANGED,
    USER_ROLE_CHANGED,
    POST_FORCE_DELETED,
    COMMENT_FORCE_DELETED
  }

  public enum TargetType {
    USER,
    POST,
    COMMENT,
    SYSTEM
  }
}
