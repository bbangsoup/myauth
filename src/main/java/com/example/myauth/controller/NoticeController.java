package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.post.PostCreateRequest;
import com.example.myauth.dto.post.PostListResponse;
import com.example.myauth.dto.post.PostResponse;
import com.example.myauth.entity.User;
import com.example.myauth.service.PostService;
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
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

  private final PostService postService;

  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createNotice(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody PostCreateRequest request
  ) {
    log.info("알림글 작성 요청 - userId: {}", user.getId());

    PostResponse response = postService.createNoticePost(user.getId(), request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("알림글이 작성되었습니다.", response));
  }

  @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PostResponse>> createNoticeWithImages(
      @AuthenticationPrincipal User user,
      @Valid @RequestPart("post") PostCreateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    log.info("알림글 작성 요청(이미지 포함) - userId: {}, imageCount: {}",
        user.getId(), images != null ? images.size() : 0);

    PostResponse response = postService.createNoticePost(user.getId(), request, images);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("알림글이 작성되었습니다.", response));
  }

  @GetMapping
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getNotices(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> notices = postService.getNoticePosts(pageable);

    return ResponseEntity.ok(ApiResponse.success("알림글 목록 조회 성공", notices));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getNotice(
      @AuthenticationPrincipal User user,
      @PathVariable Long id
  ) {
    log.info("알림글 상세 조회 요청 - userId: {}, noticeId: {}", user.getId(), id);

    PostResponse response = postService.getNoticePost(user.getId(), id);

    return ResponseEntity.ok(ApiResponse.success("알림글 조회 성공", response));
  }
}
