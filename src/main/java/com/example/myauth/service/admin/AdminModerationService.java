package com.example.myauth.service.admin;

import com.example.myauth.entity.AdminAuditLog;
import com.example.myauth.entity.Comment;
import com.example.myauth.entity.Post;
import com.example.myauth.entity.User;
import com.example.myauth.exception.CommentNotFoundException;
import com.example.myauth.exception.PostNotFoundException;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminModerationService {

  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final AdminAuditLogService adminAuditLogService;

  @Transactional
  public void forceDeletePost(User adminUser, Long postId, String reason) {
    Post post = postRepository.findAnyByIdWithUser(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));

    if (Boolean.TRUE.equals(post.getIsDeleted())) {
      throw new IllegalArgumentException("Post is already deleted.");
    }

    Map<String, Object> before = new LinkedHashMap<>();
    before.put("isDeleted", post.getIsDeleted());
    before.put("authorId", post.getUser().getId());
    before.put("content", post.getContent());

    post.softDelete();
    postRepository.save(post);

    Map<String, Object> after = new LinkedHashMap<>();
    after.put("isDeleted", post.getIsDeleted());

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.POST_FORCE_DELETED,
        AdminAuditLog.TargetType.POST,
        postId,
        reason,
        before,
        after
    );
  }

  @Transactional
  public void forceDeleteComment(User adminUser, Long commentId, String reason) {
    Comment comment = commentRepository.findAnyByIdWithUserAndPost(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

    if (Boolean.TRUE.equals(comment.getIsDeleted())) {
      throw new IllegalArgumentException("Comment is already deleted.");
    }

    Map<String, Object> before = new LinkedHashMap<>();
    before.put("isDeleted", comment.getIsDeleted());
    before.put("authorId", comment.getUser().getId());
    before.put("postId", comment.getPost().getId());
    before.put("content", comment.getContent());

    comment.softDelete();
    commentRepository.save(comment);
    postRepository.decrementCommentCount(comment.getPost().getId());

    Map<String, Object> after = new LinkedHashMap<>();
    after.put("isDeleted", comment.getIsDeleted());
    after.put("content", comment.getContent());

    adminAuditLogService.record(
        adminUser.getId(),
        AdminAuditLog.ActionType.COMMENT_FORCE_DELETED,
        AdminAuditLog.TargetType.COMMENT,
        commentId,
        reason,
        before,
        after
    );
  }
}
