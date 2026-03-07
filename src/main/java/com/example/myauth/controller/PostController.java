package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.post.*;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 寃뚯떆湲 而⑦듃濡ㅻ윭
 * 寃뚯떆湲 CRUD API ?붾뱶?ъ씤???쒓났
 *
 * ?륚PI 紐⑸줉??
 * - POST   /api/posts           : 寃뚯떆湲 ?묒꽦
 * - PUT    /api/posts/{id}      : 寃뚯떆湲 ?섏젙
 * - DELETE /api/posts/{id}      : 寃뚯떆湲 ??젣
 * - GET    /api/posts/{id}      : 寃뚯떆湲 ?곸꽭 議고쉶
 * - GET    /api/posts           : 怨듦컻 寃뚯떆湲 紐⑸줉
 * - GET    /api/posts/me        : ??寃뚯떆湲 紐⑸줉
 * - GET    /api/users/{userId}/posts : ?뱀젙 ?ъ슜??寃뚯떆湲 紐⑸줉
 */
@Slf4j
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

  private final PostService postService;
  private static final long DUPLICATE_REQUEST_WINDOW_MS = 1000L;
  private static final Map<String, Long> RECENT_LIST_REQUESTS = new ConcurrentHashMap<>();

  // ===== 寃뚯떆湲 ?묒꽦 =====

  /**
   * 寃뚯떆湲 ?묒꽦 (?띿뒪?몃쭔)
   *
   * POST /api/posts
   * Content-Type: application/json
   *
   * ?먯슂泥??덉떆??
   * {
   *   "content": "?ㅻ뒛 留쏆엳?????癒뱀뿀?댁슂!",
   *   "visibility": "PUBLIC"
   * }
   */
  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody PostCreateRequest request
  ) {
    log.info("寃뚯떆湲 ?묒꽦 ?붿껌 - userId: {}", user.getId());

    PostResponse response = postService.createPost(user.getId(), request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("寃뚯떆湲???묒꽦?섏뿀?듬땲??", response));
  }

  /**
   * 寃뚯떆湲 ?묒꽦 (?대?吏 ?ы븿)
   *
   * POST /api/posts/with-images
   * Content-Type: multipart/form-data
   *
   * ?먯슂泥??뚮씪誘명꽣??
   * - post: JSON ?뺤떇??寃뚯떆湲 ?뺣낫 (PostCreateRequest)
   * - images: ?대?吏 ?뚯씪??(?좏깮)
   */
  @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<PostResponse>> createPostWithImages(
      @AuthenticationPrincipal User user,
      @Valid @RequestPart("post") PostCreateRequest request,
      @RequestPart(value = "images", required = false) List<MultipartFile> images
  ) {
    log.info("寃뚯떆湲 ?묒꽦 ?붿껌 (?대?吏 ?ы븿) - userId: {}, ?대?吏 媛쒖닔: {}",
        user.getId(), images != null ? images.size() : 0);

    PostResponse response = postService.createPost(user.getId(), request, images);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("寃뚯떆湲???묒꽦?섏뿀?듬땲??", response));
  }

  // ===== 寃뚯떆湲 ?섏젙 =====

  /**
   * 寃뚯떆湲 ?섏젙
   *
   * PUT /api/posts/{id}
   *
   * ?먭텒?쒌?
   * - ?묒꽦??蹂몄씤留??섏젙 媛??
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long id,
      @Valid @RequestBody PostUpdateRequest request
  ) {
    log.info("寃뚯떆湲 ?섏젙 ?붿껌 - userId: {}, postId: {}", user.getId(), id);

    PostResponse response = postService.updatePost(user.getId(), id, request);

    return ResponseEntity.ok(ApiResponse.success("寃뚯떆湲???섏젙?섏뿀?듬땲??", response));
  }

  // ===== 寃뚯떆湲 ??젣 =====

  /**
   * 寃뚯떆湲 ??젣 (Soft Delete)
   *
   * DELETE /api/posts/{id}
   *
   * ?먭텒?쒌?
   * - ?묒꽦??蹂몄씤留???젣 媛??
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deletePost(
      @AuthenticationPrincipal User user,
      @PathVariable Long id
  ) {
    log.info("寃뚯떆湲 ??젣 ?붿껌 - userId: {}, postId: {}", user.getId(), id);

    postService.deletePost(user.getId(), id);

    return ResponseEntity.ok(ApiResponse.success("寃뚯떆湲????젣?섏뿀?듬땲??", null));
  }

  // ===== 寃뚯떆湲 議고쉶 =====

  /**
   * 寃뚯떆湲 ?곸꽭 議고쉶
   *
   * GET /api/posts/{id}
   *
   * ?먯쓳?듐?
   * - 寃뚯떆湲 ?뺣낫 (?묒꽦?? ?대?吏 ?ы븿)
   * - 醫뗭븘??遺곷쭏???щ? (濡쒓렇???ъ슜??湲곗?)
   * - 議고쉶???먮룞 利앷? (?묒꽦??蹂몄씤 ?쒖쇅)
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getPost(
      @AuthenticationPrincipal User user,
      HttpServletRequest request,
      @PathVariable Long id
  ) {
    log.info("寃뚯떆湲 ?곸꽭 議고쉶 ?붿껌 - userId: {}, postId: {}", user.getId(), id);

    PostResponse response = postService.getPost(user.getId(), id, request);

    return ResponseEntity.ok(ApiResponse.success("寃뚯떆湲 議고쉶 ?깃났", response));
  }

  // ===== 寃뚯떆湲 紐⑸줉 議고쉶 =====

  /**
   * 怨듦컻 寃뚯떆湲 紐⑸줉 議고쉶 (?쇰뱶)
   *
   * GET /api/posts?page=0&size=10
   *
   * ?먯옘由??뚮씪誘명꽣??
   * - page: ?섏씠吏 踰덊샇 (0遺???쒖옉, 湲곕낯媛?0)
   * - size: ?섏씠吏 ?ш린 (湲곕낯媛?10, 理쒕? 50)
   */
  @GetMapping
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getPosts(
      HttpServletRequest request,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (isDuplicateListRequest(request, page, size)) {
      return ResponseEntity
          .status(HttpStatus.TOO_MANY_REQUESTS)
          .body(ApiResponse.error("동일한 목록 요청이 너무 빠르게 반복되었습니다. 잠시 후 다시 시도해주세요."));
    }

    // ?섏씠吏 ?ш린 ?쒗븳
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getPublicPosts(pageable);

    return ResponseEntity.ok(ApiResponse.success("寃뚯떆湲 紐⑸줉 議고쉶 ?깃났", posts));
  }

  /**
   * ??寃뚯떆湲 紐⑸줉 議고쉶
   *
   * GET /api/posts/me?page=0&size=10
   */
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getMyPosts(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getMyPosts(user.getId(), pageable);

    return ResponseEntity.ok(ApiResponse.success("??寃뚯떆湲 紐⑸줉 議고쉶 ?깃났", posts));
  }

  /**
   * ?뱀젙 ?ъ슜?먯쓽 寃뚯떆湲 紐⑸줉 議고쉶
   *
   * GET /api/users/{userId}/posts?page=0&size=10
   *
   * ?먯갭怨졼?
   * - ???붾뱶?ъ씤?몃뒗 UserController???꾩튂???섎룄 ?덉쓬
   * - ?ш린?쒕뒗 寃뚯떆湲 愿?⑥씠誘濡?PostController??諛곗튂
   */
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<Page<PostListResponse>>> getUserPosts(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    if (size > 50) size = 50;

    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    Page<PostListResponse> posts = postService.getPostsByUser(userId, pageable);

    return ResponseEntity.ok(ApiResponse.success("?ъ슜??寃뚯떆湲 紐⑸줉 議고쉶 ?깃났", posts));
  }
  private boolean isDuplicateListRequest(HttpServletRequest request, int page, int size) {
    String key = String.format("%s|%s|%d|%d",
        resolveClientIp(request),
        request.getRequestURI(),
        page,
        size);

    long now = System.currentTimeMillis();
    Long previous = RECENT_LIST_REQUESTS.put(key, now);
    if (previous == null) {
      return false;
    }
    return now - previous < DUPLICATE_REQUEST_WINDOW_MS;
  }

  private String resolveClientIp(HttpServletRequest request) {
    String forwarded = request.getHeader("X-Forwarded-For");
    if (forwarded == null || forwarded.isBlank()) {
      return request.getRemoteAddr();
    }
    return forwarded.split(",")[0].trim();
  }

}
