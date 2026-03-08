package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@DynamicInsert
@Table(name = "dm_messages",
    indexes = {
        @Index(name = "idx_dm_messages_room_id_id", columnList = "room_id, id DESC"),
        @Index(name = "idx_dm_messages_sender_created_at", columnList = "sender_id, created_at DESC")
    }
)
public class DmMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_messages_room"))
  private DmRoom room;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false, foreignKey = @ForeignKey(name = "fk_dm_messages_sender"))
  private User sender;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "is_deleted", nullable = false)
  @ColumnDefault("false")
  @Builder.Default
  private Boolean isDeleted = false;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public static DmMessage create(DmRoom room, User sender, String content) {
    return DmMessage.builder()
        .room(room)
        .sender(sender)
        .content(content)
        .isDeleted(false)
        .build();
  }
}
