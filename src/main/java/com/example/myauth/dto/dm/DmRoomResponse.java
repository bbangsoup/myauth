package com.example.myauth.dto.dm;

import com.example.myauth.entity.DmRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmRoomResponse {

  private Long roomId;
  private Long user1Id;
  private Long user2Id;
  private Long lastMessageId;
  private LocalDateTime lastMessageAt;
  private LocalDateTime createdAt;
  private boolean created;

  public static DmRoomResponse from(DmRoom room, boolean created) {
    return DmRoomResponse.builder()
        .roomId(room.getId())
        .user1Id(room.getUser1().getId())
        .user2Id(room.getUser2().getId())
        .lastMessageId(room.getLastMessageId())
        .lastMessageAt(room.getLastMessageAt())
        .createdAt(room.getCreatedAt())
        .created(created)
        .build();
  }
}
