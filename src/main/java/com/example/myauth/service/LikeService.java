package com.example.myauth.service;

import com.example.myauth.dto.like.LikeResponse;
import com.example.myauth.dto.like.LikeUserResponse;
import com.example.myauth.entity.Like;
import com.example.myauth.entity.Post;
import com.example.myauth.entity.User;
import com.example.myauth.exception.CommentNotFoundException;
// import com.example.myauth.exception.DuplicateLikeException;
// import com.example.myauth.exception.LikeNotFoundException;
import com.example.myauth.exception.PostNotFoundException;
import com.example.myauth.repository.CommentRepository;
import com.example.myauth.repository.LikeRepository;
import com.example.myauth.repository.PostRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 좋아요 서비스
 * 게시글/댓글에 대한 좋아요 비즈니스 로직 처리
 *
 * 【주요 기능】
 * - 게시글 좋아요/좋아요 취소
 * - 댓글 좋아요/좋아요 취소
 * - 좋아요 누른 사용자 목록 조회
 */
@Slf4j

@Service
@RequiredArgsConstructor
public class LikeService {

  private final LikeRepository likeRepository;
  private final PostRepository postRepository;
  private final CommentRepository commentRepository;
  private final UserRepository userRepository;

  // ===== 게시글 좋아요 =====

  /**
   * 게시글 좋아요
   *
   * @param userId 사용자 ID
   * @param postId 게시글 ID
   * @return 좋아요 응답
   */
  @Transactional
  public LikeResponse likePost(Long userId, Long postId) {
    log.info("게시글 좋아요 - userId: {}, postId: {}", userId, postId);

    // 1. 게시글 존재 확인
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));

      // 2. 이미 좋아요 상태면 그대로 성공 반환 (멱등)
      if (likeRepository.existsPostLikeByUserId(userId, postId)) {
          return LikeResponse.forPost(postId, true, post.getLikeCount());
      }

    // 3. 사용자 조회
    User user = userRepository.getReferenceById(userId);

    // 4. 좋아요 저장
    Like like = Like.forPost(user, postId);
    likeRepository.save(like);

    // 5. 게시글 좋아요 수 증가
    postRepository.incrementLikeCount(postId);

    // 6. 현재 좋아요 수 조회
    int likeCount = post.getLikeCount() + 1;

    log.info("게시글 좋아요 완료 - postId: {}, likeCount: {}", postId, likeCount);

    return LikeResponse.forPost(postId, true, likeCount);
  }

  /**
   * 게시글 좋아요 취소
   *
   * @param userId 사용자 ID
   * @param postId 게시글 ID
   * @return 좋아요 응답
   */
  @Transactional
  public LikeResponse unlikePost(Long userId, Long postId) {
    log.info("게시글 좋아요 취소 - userId: {}, postId: {}", userId, postId);

    // 1. 게시글 존재 확인
    Post post = postRepository.findByIdAndIsDeletedFalse(postId)
        .orElseThrow(() -> new PostNotFoundException(postId));

      // 2. 좋아요 기록 있으면 삭제, 없으면 그대로 성공 반환 (멱등)
      var likeOpt = likeRepository.findPostLikeByUserId(userId, postId);
      if (likeOpt.isPresent()) {
          likeRepository.delete(likeOpt.get());
          postRepository.decrementLikeCount(postId);

          int likeCount = Math.max(0, post.getLikeCount() - 1);
          log.info("게시글 좋아요 취소 완료 - postId: {}, likeCount: {}", postId, likeCount);
          return LikeResponse.forPost(postId, false, likeCount);
      }

      return LikeResponse.forPost(postId, false, post.getLikeCount());
  }

  // ===== 댓글 좋아요 =====

  /**
   * 댓글 좋아요
   *
   * @param userId 사용자 ID
   * @param commentId 댓글 ID
   * @return 좋아요 응답
   */
  @Transactional
  public LikeResponse likeComment(Long userId, Long commentId) {
    log.info("댓글 좋아요 - userId: {}, commentId: {}", userId, commentId);

    // 1. 댓글 존재 확인
    var comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

      // 2. 이미 좋아요 상태면 그대로 성공 반환 (멱등)
      if (likeRepository.existsCommentLikeByUserId(userId, commentId)) {
          return LikeResponse.forComment(commentId, true, comment.getLikeCount());
      }

    // 3. 사용자 조회
    User user = userRepository.getReferenceById(userId);

    // 4. 좋아요 저장
    Like like = Like.forComment(user, commentId);
    likeRepository.save(like);

    // 5. 댓글 좋아요 수 증가
    commentRepository.incrementLikeCount(commentId);

    // 6. 현재 좋아요 수 계산
    int likeCount = comment.getLikeCount() + 1;

    log.info("댓글 좋아요 완료 - commentId: {}, likeCount: {}", commentId, likeCount);

    return LikeResponse.forComment(commentId, true, likeCount);
  }

  /**
   * 댓글 좋아요 취소
   *
   * @param userId 사용자 ID
   * @param commentId 댓글 ID
   * @return 좋아요 응답
   */
  @Transactional
  public LikeResponse unlikeComment(Long userId, Long commentId) {
    log.info("댓글 좋아요 취소 - userId: {}, commentId: {}", userId, commentId);

    // 1. 댓글 존재 확인
    var comment = commentRepository.findByIdAndIsDeletedFalse(commentId)
        .orElseThrow(() -> new CommentNotFoundException(commentId));

      // 2. 좋아요 기록 있으면 삭제, 없으면 그대로 성공 반환 (멱등)
      var likeOpt = likeRepository.findCommentLikeByUserId(userId, commentId);
      if (likeOpt.isPresent()) {
          likeRepository.delete(likeOpt.get());
          commentRepository.decrementLikeCount(commentId);

          int likeCount = Math.max(0, comment.getLikeCount() - 1);
          log.info("댓글 좋아요 취소 완료 - commentId: {}, likeCount: {}", commentId, likeCount);
          return LikeResponse.forComment(commentId, false, likeCount);
      }

      return LikeResponse.forComment(commentId, false, comment.getLikeCount());
  }

  // ===== 좋아요 여부 확인 =====

  /**
   * 게시글 좋아요 여부 확인
   *
   * @param userId 사용자 ID
   * @param postId 게시글 ID
   * @return 좋아요 여부
   */
  @Transactional(readOnly = true)
  public boolean isPostLiked(Long userId, Long postId) {
    return likeRepository.existsPostLikeByUserId(userId, postId);
  }

  /**
   * 댓글 좋아요 여부 확인
   *
   * @param userId 사용자 ID
   * @param commentId 댓글 ID
   * @return 좋아요 여부
   */
  @Transactional(readOnly = true)
  public boolean isCommentLiked(Long userId, Long commentId) {
    return likeRepository.existsCommentLikeByUserId(userId, commentId);
  }

  // ===== 좋아요 누른 사용자 목록 =====

  /**
   * 게시글에 좋아요 누른 사용자 목록 조회
   *
   * @param postId 게시글 ID
   * @param pageable 페이지 정보
   * @return 사용자 목록 페이지
   */
  @Transactional(readOnly = true)
  public Page<LikeUserResponse> getPostLikeUsers(Long postId, Pageable pageable) {
    log.info("게시글 좋아요 사용자 목록 조회 - postId: {}", postId);

    // 게시글 존재 확인
    if (!postRepository.existsById(postId)) {
      throw new PostNotFoundException(postId);
    }

    Page<User> users = likeRepository.findUsersWhoLikedPost(postId, pageable);
    return users.map(LikeUserResponse::from);
  }

  /**
   * 댓글에 좋아요 누른 사용자 목록 조회
   *
   * @param commentId 댓글 ID
   * @param pageable 페이지 정보
   * @return 사용자 목록 페이지
   */
  @Transactional(readOnly = true)
  public Page<LikeUserResponse> getCommentLikeUsers(Long commentId, Pageable pageable) {
    log.info("댓글 좋아요 사용자 목록 조회 - commentId: {}", commentId);

    // 댓글 존재 확인
    if (!commentRepository.existsById(commentId)) {
      throw new CommentNotFoundException(commentId);
    }

    Page<User> users = likeRepository.findUsersWhoLikedComment(commentId, pageable);
    return users.map(LikeUserResponse::from);
  }
}
