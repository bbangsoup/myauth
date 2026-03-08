package com.example.myauth.service;

import com.example.myauth.dto.dm.DmMessageCreateRequest;
import com.example.myauth.dto.dm.DmMessageResponse;
import com.example.myauth.dto.dm.DmRoomDetailResponse;
import com.example.myauth.dto.dm.DmRoomListItemResponse;
import com.example.myauth.dto.dm.DmRoomResponse;
import com.example.myauth.entity.DmMessage;
import com.example.myauth.entity.DmRoom;
import com.example.myauth.entity.User;
import com.example.myauth.exception.DmAccessDeniedException;
import com.example.myauth.exception.DmMessageValidationException;
import com.example.myauth.exception.DmPolicyViolationException;
import com.example.myauth.exception.DmRoomNotFoundException;
import com.example.myauth.exception.UserNotFoundException;
import com.example.myauth.repository.DmMessageRepository;
import com.example.myauth.repository.DmRoomRepository;
import com.example.myauth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Slice;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DmService {

  private final DmRoomRepository dmRoomRepository;
  private final DmMessageRepository dmMessageRepository;
  private final UserRepository userRepository;

  @Transactional
  public DmRoomResponse createOrGetRoom(Long meId, Long targetUserId) {
    if (meId.equals(targetUserId)) {
      throw new DmPolicyViolationException("본인에게 DM 방을 만들 수 없습니다.");
    }

    User me = userRepository.findById(meId)
        .orElseThrow(() -> new UserNotFoundException(meId));
    User target = userRepository.findById(targetUserId)
        .orElseThrow(() -> new UserNotFoundException(targetUserId));

    Long user1Id = Math.min(meId, targetUserId);
    Long user2Id = Math.max(meId, targetUserId);

    return dmRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
        .map(room -> DmRoomResponse.from(room, false))
        .orElseGet(() -> createRoomWithUniquenessGuard(me, target, user1Id, user2Id));
  }

  @Transactional
  public DmMessageResponse sendMessage(Long senderId, Long roomId, DmMessageCreateRequest request) {
    DmRoom room = getAccessibleRoom(senderId, roomId);

    User sender = userRepository.findById(senderId)
        .orElseThrow(() -> new UserNotFoundException(senderId));

    String content = request.getContent() == null ? "" : request.getContent().trim();
    if (content.isEmpty()) {
      throw new DmMessageValidationException("메시지 내용은 비워둘 수 없습니다.");
    }

    DmMessage message = dmMessageRepository.save(
        DmMessage.create(room, sender, content)
    );

    room.setLastMessageId(message.getId());
    room.setLastMessageAt(message.getCreatedAt());
    dmRoomRepository.save(room);

    log.info("DM 메시지 전송 완료 - roomId: {}, senderId: {}, messageId: {}",
        roomId, senderId, message.getId());

    return DmMessageResponse.from(message);
  }

  @Transactional(readOnly = true)
  public DmRoomDetailResponse getRoomDetail(Long meId, Long roomId) {
    DmRoom room = getAccessibleRoom(meId, roomId);
    User peerUser = Objects.equals(room.getUser1().getId(), meId) ? room.getUser2() : room.getUser1();
    return DmRoomDetailResponse.from(room, peerUser);
  }

  @Transactional(readOnly = true)
  public Slice<DmMessageResponse> getMessages(Long meId, Long roomId, Long beforeId, int size) {
    getAccessibleRoom(meId, roomId);
    Pageable pageable = PageRequest.of(0, Math.min(Math.max(size, 1), 100));

    Slice<DmMessage> messages = beforeId == null
        ? dmMessageRepository.findLatestMessages(roomId, pageable)
        : dmMessageRepository.findMessagesBeforeId(roomId, beforeId, pageable);

    return messages.map(DmMessageResponse::from);
  }

  @Transactional(readOnly = true)
  public Page<DmRoomListItemResponse> getMyRooms(Long meId, int page, int size) {
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    Page<DmRoom> rooms = dmRoomRepository.findMyRooms(meId, pageable);

    return rooms.map(room -> {
      User peerUser = Objects.equals(room.getUser1().getId(), meId) ? room.getUser2() : room.getUser1();

      Optional<DmMessage> lastMessage = room.getLastMessageId() == null
          ? Optional.empty()
          : dmMessageRepository.findById(room.getLastMessageId());

      return DmRoomListItemResponse.builder()
          .roomId(room.getId())
          .peerUserId(peerUser.getId())
          .peerUserName(peerUser.getName())
          .peerProfileImage(peerUser.getProfileImage())
          .lastMessageId(room.getLastMessageId())
          .lastMessagePreview(lastMessage.map(DmMessage::getContent).orElse(null))
          .lastMessageAt(room.getLastMessageAt())
          .build();
    });
  }

  private DmRoomResponse createRoomWithUniquenessGuard(User me, User target, Long user1Id, Long user2Id) {
    try {
      DmRoom saved = dmRoomRepository.save(DmRoom.create(me, target));
      return DmRoomResponse.from(saved, true);
    } catch (DataIntegrityViolationException ex) {
      DmRoom existing = dmRoomRepository.findByUser1IdAndUser2Id(user1Id, user2Id)
          .orElseThrow(() -> ex);
      return DmRoomResponse.from(existing, false);
    }
  }

  private DmRoom getAccessibleRoom(Long meId, Long roomId) {
    if (!dmRoomRepository.existsById(roomId)) {
      throw new DmRoomNotFoundException(roomId);
    }
    return dmRoomRepository.findAccessibleRoom(roomId, meId)
        .orElseThrow(DmAccessDeniedException::new);
  }
}
