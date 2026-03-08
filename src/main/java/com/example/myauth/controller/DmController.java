package com.example.myauth.controller;

import com.example.myauth.dto.ApiResponse;
import com.example.myauth.dto.dm.DmMessageCreateRequest;
import com.example.myauth.dto.dm.DmMessageResponse;
import com.example.myauth.dto.dm.DmRoomCreateRequest;
import com.example.myauth.dto.dm.DmRoomDetailResponse;
import com.example.myauth.dto.dm.DmRoomListItemResponse;
import com.example.myauth.dto.dm.DmRoomResponse;
import com.example.myauth.entity.User;
import com.example.myauth.service.DmService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Page;
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

  /**
   * 1:1 DM 방을 생성하거나 기존 방을 조회한다.
   *
   * @param user 인증된 요청 사용자
   * @param request DM 방 생성 요청 DTO
   * @return ApiResponse<DmRoomResponse>
   */
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

  /**
   * 특정 DM 방에 메시지 1건을 전송한다.
   *
   * @param user 인증된 발신자 사용자
   * @param roomId 메시지를 전송할 DM 방 ID
   * @param request 메시지 전송 요청 DTO
   * @return ApiResponse<DmMessageResponse>
   */
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

  /**
   * 내 DM 방 목록을 조회한다.
   *
   * @param user 인증된 요청 사용자
   * @param page 페이지 번호(기본 0)
   * @param size 페이지 크기(기본 20, 최대 100)
   * @return ApiResponse<Page<DmRoomListItemResponse>>
   */
  @GetMapping("/rooms")
  public ResponseEntity<ApiResponse<Page<DmRoomListItemResponse>>> getMyRooms(
      @AuthenticationPrincipal User user,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size
  ) {
    Page<DmRoomListItemResponse> response = dmService.getMyRooms(user.getId(), page, size);
    return ResponseEntity.ok(ApiResponse.success("DM 방 목록을 조회했습니다.", response));
  }

  /**
   * DM 방 상세(대화 상대 정보 포함)를 조회한다.
   *
   * @param user 인증된 요청 사용자
   * @param roomId DM 방 ID
   * @return ApiResponse<DmRoomDetailResponse>
   */
  @GetMapping("/rooms/{roomId}")
  public ResponseEntity<ApiResponse<DmRoomDetailResponse>> getRoomDetail(
      @AuthenticationPrincipal User user,
      @PathVariable Long roomId
  ) {
    DmRoomDetailResponse response = dmService.getRoomDetail(user.getId(), roomId);
    return ResponseEntity.ok(ApiResponse.success("DM 방 정보를 조회했습니다.", response));
  }

  /**
   * DM 메시지 목록을 조회한다. (커서 기반 beforeId)
   *
   * @param user 인증된 요청 사용자
   * @param roomId DM 방 ID
   * @param beforeId 해당 ID보다 작은 메시지를 조회할 커서
   * @param size 조회 개수(기본 30, 최대 100)
   * @return ApiResponse<Slice<DmMessageResponse>>
   */
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
