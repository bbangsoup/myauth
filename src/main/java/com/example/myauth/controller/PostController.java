package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.post.PostCreateRequest;
import com.example.myauth.dto.post.PostListResponse;
import com.example.myauth.dto.post.PostResponse;
import com.example.myauth.dto.post.PostUpdateRequest;
import com.example.myauth.entity.User;
import com.example.myauth.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody PostCreateRequest request
  ) {
    log.info("게시글 생성 요청 - userId: {}", user.getId());
    PostResponse response = postService.createPost(user.getId(), request);
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("게시글이 생성되었습니다.", response));
  }

  @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PostResponse>> createPostWithImages(
      @AuthenticationPrincipal User user,
      @Valid @RequestPart("post") PostCreateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    log.info("게시글 생성 요청(이미지 포함) - userId: {}, imageCount: {}",
        user.getId(), images != null ? images.size() : 0);

    PostResponse response = postService.createPost(user.getId(), request, images);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("게시글이 생성되었습니다.", response));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @Valid @RequestBody PostUpdateRequest updateRequest
  ) {
    log.info("게시글 수정 요청 - userId: {}, postId: {}", user.getId(), id);

    PostResponse response = postService.updatePost(user.getId(), id, updateRequest);

    return ResponseEntity.ok(ApiResponse.success("게시글이 수정되었습니다.", response));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long id
  ) {
    log.info("게시글 삭제 요청 - userId: {}, postId: {}", user.getId(), id);

    postService.deletePost(user.getId(), id);

    return ResponseEntity.ok(ApiResponse.success("게시글이 삭제되었습니다.", null));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getPost(
      @AuthenticationPrincipal User user,
      HttpServletRequest request,
      @PathVariable Long id
  ) {
    log.info("게시글 상세 조회 요청 - userId: {}, postId: {}", user.getId(), id);

    PostResponse response = postService.getPost(user.getId(), id, request);

    return ResponseEntity.ok(ApiResponse.success("게시글 조회 성공", response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPosts(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) {
      size = 50;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getPublicPosts(pageable);

    return ResponseEntity.ok(ApiResponse.success("게시글 목록 조회 성공", posts));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getMyPosts(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) {
      size = 50;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getMyPosts(user.getId(), pageable);

    return ResponseEntity.ok(ApiResponse.success("내 게시글 목록 조회 성공", posts));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getUserPosts(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) {
      size = 50;
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getPostsByUser(userId, pageable);

    return ResponseEntity.ok(ApiResponse.success("사용자 게시글 목록 조회 성공", posts));
  }
}