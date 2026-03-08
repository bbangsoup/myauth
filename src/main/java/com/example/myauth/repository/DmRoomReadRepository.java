package com.example.myauth.repository;

import com.example.myauth.entity.DmMessage;
import com.example.myauth.entity.DmRoomRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface DmRoomReadRepository extends JpaRepository<DmRoomRead, Long> {

  Optional<DmRoomRead> findByRoomIdAndUserId(Long roomId, Long userId);

  boolean existsByRoomIdAndUserId(Long roomId, Long userId);

  @Modifying
  @Query("UPDATE DmRoomRead rr " +
      "SET rr.lastReadMessage = :lastReadMessage, rr.lastReadAt = :lastReadAt " +
      "WHERE rr.room.id = :roomId AND rr.user.id = :userId " +
      "AND (rr.lastReadMessage IS NULL OR rr.lastReadMessage.id < :lastReadMessageId)")
  int updateLastReadIfGreater(
      @Param("roomId") Long roomId,
      @Param("userId") Long userId,
      @Param("lastReadMessage") DmMessage lastReadMessage,
      @Param("lastReadMessageId") Long lastReadMessageId,
      @Param("lastReadAt") LocalDateTime lastReadAt
  );
}
