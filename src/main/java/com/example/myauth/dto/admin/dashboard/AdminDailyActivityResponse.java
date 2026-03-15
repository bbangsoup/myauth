package com.example.myauth.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminDailyActivityResponse {
  private String date;
  private long newUsers;
  private long newPosts;
  private long newComments;
}
