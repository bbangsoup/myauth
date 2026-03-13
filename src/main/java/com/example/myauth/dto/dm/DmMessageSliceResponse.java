package com.example.myauth.dto.dm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageSliceResponse {

  private List<DmMessageResponse> messages;
  private boolean hasNext;
  private Long nextCursor;
  private Long unreadCountInRoom;
  private Long unreadCountInPage;
  private Long firstUnreadMessageIdInPage;
}