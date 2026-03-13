package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.dm.DmMessageCreateRequest;
import com.example.myauth.dto.dm.DmMessageResponse;
import com.example.myauth.dto.dm.DmMessageSliceResponse;
import com.example.myauth.dto.dm.DmRoomCreateRequest;
import com.example.myauth.dto.dm.DmRoomDetailResponse;
import com.example.myauth.dto.dm.DmRoomListItemResponse;
import com.example.myauth.dto.dm.DmRoomResponse;
import com.example.myauth.dto.dm.DmUnreadCountResponse;
import com.example.myauth.entity.User;
import com.example.myauth.service.DmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/dm")
@RequiredArgsConstructor
public class DmController {

  private final DmService dmService;

  @PostMapping("/rooms")
  public ResponseEntity<ApiResponse<DmRoomResponse>> createOrGetRoom(
      @AuthenticationPrincipal User user,
      @Valid @RequestBody DmRoomCreateRequest request
  ) {
    DmRoomResponse response = dmService.createOrGetRoom(user.getId(), request.getTargetUserId());

    HttpStatus status = response.isCreated() ? HttpStatus.CREATED : HttpStatus.OK;
    String message = response.isCreated() ? "DM 방을 생성했습니다." : "기존 DM 방을 조회했습니다.";

    log.info("DM room ready - requesterId: {}, targetUserId: {}, roomId: {}, created: {}",
        user.getId(), request.getTargetUserId(), response.getRoomId(), response.isCreated());

    return ResponseEntity.status(status).body(ApiResponse.success(message, response));
  }

  @PostMapping("/rooms/{roomId}/messages")
  public ResponseEntity<ApiResponse<DmMessageResponse>> sendMessage(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @Valid @RequestBody DmMessageCreateRequest request
  ) {
    DmMessageResponse response = dmService.sendMessage(user.getId(), roomId, request);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.success("메시지를 전송했습니다.", response));
  }

  @GetMapping("/rooms")
  public ResponseEntity<ApiResponse<Page<DmRoomListItemResponse>>> getMyRooms(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Page<DmRoomListItemResponse> response = dmService.getMyRooms(user.getId(), page, size);
    return ResponseEntity.ok(ApiResponse.success("DM 방 목록을 조회했습니다.", response));
  }

  @GetMapping("/unread-count")
  public ResponseEntity<ApiResponse<DmUnreadCountResponse>> getUnreadCount(
      @AuthenticationPrincipal User user
  ) {
    DmUnreadCountResponse response = dmService.getUnreadCount(user.getId());
    return ResponseEntity.ok(ApiResponse.success("미읽음 DM 개수를 조회했습니다.", response));
  }

  @GetMapping("/rooms/{roomId}")
  public ResponseEntity<ApiResponse<DmRoomDetailResponse>> getRoomDetail(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId
  ) {
    DmRoomDetailResponse response = dmService.getRoomDetail(user.getId(), roomId);
    return ResponseEntity.ok(ApiResponse.success("DM 방 정보를 조회했습니다.", response));
  }

  @GetMapping("/rooms/{roomId}/messages-with-unread")
  public ResponseEntity<ApiResponse<DmMessageSliceResponse>> getMessagesWithUnread(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @RequestParam(required = false) Long beforeId,
      @RequestParam(defaultValue = "30") int size
  ) {
    DmMessageSliceResponse response = dmService.getMessagesWithUnreadMeta(user.getId(), roomId, beforeId, size);
    return ResponseEntity.ok(ApiResponse.success("DM 메시지 목록과 미읽음 정보를 조회했습니다.", response));
  }

  @GetMapping("/rooms/{roomId}/messages")
  public ResponseEntity<ApiResponse<Slice<DmMessageResponse>>> getMessages(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId,
      @RequestParam(required = false) Long beforeId,
      @RequestParam(defaultValue = "30") int size
  ) {
    Slice<DmMessageResponse> response = dmService.getMessages(user.getId(), roomId, beforeId, size);
    return ResponseEntity.ok(ApiResponse.success("DM 메시지 목록을 조회했습니다.", response));
  }
}