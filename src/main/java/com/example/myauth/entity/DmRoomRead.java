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
@Table(name = "dm_room_reads",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_dm_room_reads_room_user",
            columnNames = {"room_id", "user_id"}
        )
    },
    indexes = {
        @Index(name = "idx_dm_room_reads_user_updated", columnList = "user_id, updated_at DESC")
    }
)
public class DmRoomRead {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_room_reads_room"))
  private DmRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_room_reads_user"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_read_message_id", foreignKey = @ForeignKey(name = "fk_dm_room_reads_last_message"))
  private DmMessage lastReadMessage;

  @Column(name = "last_read_at")
  private LocalDateTime lastReadAt;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
