package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.example.myauth.dto.admin.dashboard.AdminDailyActivityResponse;
import com.example.myauth.entity.User;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly = true)
  public AdminDashboardSummaryResponse getSummary() {
    LocalDate today = LocalDate.now();
    LocalDateTime startOfToday = today.atStartOfDay();
    LocalDate trendStartDate = today.minusDays(29);
    LocalDateTime trendStart = trendStartDate.atStartOfDay();

    long totalUsers = userRepository.count();
    long activeUsers = userRepository.countByStatus(User.Status.ACTIVE);
    long suspendedUsers = userRepository.countByStatus(User.Status.SUSPENDED);
    long adminUsers = userRepository.countByRole(User.Role.ROLE_ADMIN);

    long activePosts = postRepository.countByIsDeletedFalse();
    long deletedPosts = postRepository.countByIsDeletedTrue();
    long totalPosts = activePosts + deletedPosts;

    long activeComments = commentRepository.countByIsDeletedFalse();
    long deletedComments = commentRepository.countByIsDeletedTrue();
    long totalComments = activeComments + deletedComments;

    Map<String, Long> dailyUsers = toCountMap(userRepository.findCreatedAtAfter(trendStart));
    Map<String, Long> dailyPosts = toCountMap(postRepository.findCreatedAtAfter(trendStart));
    Map<String, Long> dailyComments = toCountMap(commentRepository.findCreatedAtAfter(trendStart));

    List<AdminDailyActivityResponse> dailyStats = trendStartDate.datesUntil(today.plusDays(1))
        .map(date -> {
          String key = date.toString();
          return AdminDailyActivityResponse.builder()
              .date(key)
              .newUsers(dailyUsers.getOrDefault(key, 0L))
              .newPosts(dailyPosts.getOrDefault(key, 0L))
              .newComments(dailyComments.getOrDefault(key, 0L))
              .build();
        })
        .toList();

    return AdminDashboardSummaryResponse.builder()
        .totalUsers(totalUsers)
        .activeUsers(activeUsers)
        .suspendedUsers(suspendedUsers)
        .adminUsers(adminUsers)
        .totalPosts(totalPosts)
        .activePosts(activePosts)
        .deletedPosts(deletedPosts)
        .totalComments(totalComments)
        .activeComments(activeComments)
        .deletedComments(deletedComments)
        .newUsersToday(userRepository.countByCreatedAtAfter(startOfToday))
        .newPostsToday(postRepository.countByCreatedAtAfter(startOfToday))
        .newCommentsToday(commentRepository.countByCreatedAtAfter(startOfToday))
        .dailyStats(dailyStats)
        .build();
  }

  private Map<String, Long> toCountMap(List<LocalDateTime> timestamps) {
    return timestamps.stream()
        .collect(Collectors.groupingBy(
            timestamp -> timestamp.toLocalDate().toString(),
            Collectors.counting()
        ));
  }
}
