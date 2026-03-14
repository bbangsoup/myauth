package com.example.myauth.service.admin;

import com.example.myauth.dto.admin.dashboard.AdminDashboardSummaryResponse;
import com.example.myauth.entity.User;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

  private final UserRepository userRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;

  @Transactional(readOnly = true)
  public AdminDashboardSummaryResponse getSummary() {
    LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

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
        .build();
  }
}
