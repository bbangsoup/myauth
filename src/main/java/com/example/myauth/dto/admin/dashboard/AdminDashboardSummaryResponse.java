package com.example.myauth.dto.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AdminDashboardSummaryResponse {
  private long totalUsers;
  private long activeUsers;
  private long suspendedUsers;
  private long adminUsers;
  private long totalPosts;
  private long activePosts;
  private long deletedPosts;
  private long totalComments;
  private long activeComments;
  private long deletedComments;
  private long newUsersToday;
  private long newPostsToday;
  private long newCommentsToday;
}
