package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "dm_rooms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_dm_rooms_user_pair",
            columnNames = {"user1_id", "user2_id"}
        )
    },
    indexes = {
        @Index(name = "idx_dm_rooms_last_message_at", columnList = "last_message_at DESC"),
        @Index(name = "idx_dm_rooms_user1_id", columnList = "user1_id"),
        @Index(name = "idx_dm_rooms_user2_id", columnList = "user2_id")
    }
)
public class DmRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user1_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_rooms_user1"))
  private User user1;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user2_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_rooms_user2"))
  private User user2;

  @Column(name = "last_message_id")
  private Long lastMessageId;

  @Column(name = "last_message_at")
  private LocalDateTime lastMessageAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public static DmRoom create(User a, User b) {
    if (a == null || b == null || a.getId() == null || b.getId() == null) {
      throw new IllegalArgumentException("Both users must exist before creating a DM room.");
    }

    if (a.getId() < b.getId()) {
      return DmRoom.builder()
          .user1(a)
          .user2(b)
          .build();
    }

    return DmRoom.builder()
        .user1(b)
        .user2(a)
        .build();
  }

  @PrePersist
  @PreUpdate
  public void validateAndNormalizeUserOrder() {
    if (user1 == null || user2 == null || user1.getId() == null || user2.getId() == null) {
      throw new IllegalStateException("user1 and user2 must be set with persisted users.");
    }

    if (user1.getId().equals(user2.getId())) {
      throw new IllegalArgumentException("A DM room cannot be created with the same user.");
    }

    if (user1.getId() > user2.getId()) {
      User temp = user1;
      user1 = user2;
      user2 = temp;
    }
  }
}
