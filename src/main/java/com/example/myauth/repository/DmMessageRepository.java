package com.example.myauth.repository;

import com.example.myauth.entity.DmMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DmMessageRepository extends JpaRepository<DmMessage, Long> {

  Optional<DmMessage> findTopByRoomIdOrderByIdDesc(Long roomId);

  @Query("SELECT m FROM DmMessage m " +
      "WHERE m.room.id = :roomId " +
      "ORDER BY m.id DESC")
  Slice<DmMessage> findLatestMessages(@Param("roomId") Long roomId, Pageable pageable);

  @Query("SELECT m FROM DmMessage m " +
      "WHERE m.room.id = :roomId AND m.id < :beforeId " +
      "ORDER BY m.id DESC")
  Slice<DmMessage> findMessagesBeforeId(
      @Param("roomId") Long roomId,
      @Param("beforeId") Long beforeId,
      Pageable pageable
  );

  @Query("SELECT COUNT(m) FROM DmMessage m " +
      "WHERE m.room.id = :roomId AND m.id > :lastReadMessageId " +
      "AND m.isDeleted = false AND m.sender.id <> :userId")
  long countUnreadMessagesForUser(
      @Param("roomId") Long roomId,
      @Param("lastReadMessageId") Long lastReadMessageId,
      @Param("userId") Long userId
  );

  @Query("SELECT COUNT(m) FROM DmMessage m " +
      "WHERE m.room.id = :roomId AND m.isDeleted = false AND m.sender.id <> :userId")
  long countUnreadMessagesWhenNoReadMarkerForUser(
      @Param("roomId") Long roomId,
      @Param("userId") Long userId
  );

  @Query("SELECT m.id FROM DmMessage m " +
      "WHERE m.room.id = :roomId AND m.id IN :messageIds")
  List<Long> findExistingMessageIdsInRoom(
      @Param("roomId") Long roomId,
      @Param("messageIds") List<Long> messageIds
  );
}
