package com.example.myauth.dto.dm;

import com.example.myauth.entity.DmMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageResponse {

  private Long messageId;
  private Long roomId;
  private Long senderId;
  private String senderName;
  private String senderProfileImage;
  private String content;
  private Boolean isDeleted;
  private LocalDateTime createdAt;

  public static DmMessageResponse from(DmMessage message) {
    return DmMessageResponse.builder()
        .messageId(message.getId())
        .roomId(message.getRoom().getId())
        .senderId(message.getSender().getId())
        .senderName(message.getSender().getName())
        .senderProfileImage(message.getSender().getProfileImage())
        .content(message.getContent())
        .isDeleted(message.getIsDeleted())
        .createdAt(message.getCreatedAt())
        .build();
  }
}
