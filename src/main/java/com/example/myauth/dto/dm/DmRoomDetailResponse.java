package com.example.myauth.dto.dm;

import com.example.myauth.entity.DmRoom;
import com.example.myauth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmRoomDetailResponse {

  private Long roomId;
  private Long peerUserId;
  private String peerUserName;
  private String peerProfileImage;
  private Long lastMessageId;
  private LocalDateTime lastMessageAt;

  public static DmRoomDetailResponse from(DmRoom room, User peerUser) {
    return DmRoomDetailResponse.builder()
        .roomId(room.getId())
        .peerUserId(peerUser.getId())
        .peerUserName(peerUser.getName())
        .peerProfileImage(peerUser.getProfileImage())
        .lastMessageId(room.getLastMessageId())
        .lastMessageAt(room.getLastMessageAt())
        .build();
  }
}
