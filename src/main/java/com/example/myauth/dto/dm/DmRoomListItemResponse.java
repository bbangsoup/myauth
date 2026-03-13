package com.example.myauth.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmRoomListItemResponse {

  private Long roomId;
  private Long peerUserId;
  private String peerUserName;
  private String peerProfileImage;
  private Long lastMessageId;
  private String lastMessagePreview;
  private LocalDateTime lastMessageAt;
  private Long unreadCount;
}
