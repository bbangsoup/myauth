package com.example.myauth.repository;

import com.example.myauth.entity.DmRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DmRoomRepository extends JpaRepository<DmRoom, Long> {

  Optional<DmRoom> findByUser1IdAndUser2Id(Long user1Id, Long user2Id);

  @Query("SELECT r FROM DmRoom r " +
      "WHERE r.id = :roomId AND (r.user1.id = :userId OR r.user2.id = :userId)")
  Optional<DmRoom> findAccessibleRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

  @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM DmRoom r " +
      "WHERE r.id = :roomId AND (r.user1.id = :userId OR r.user2.id = :userId)")
  boolean existsAccessibleRoom(@Param("roomId") Long roomId, @Param("userId") Long userId);

  @Query("SELECT r FROM DmRoom r " +
      "WHERE r.user1.id = :userId OR r.user2.id = :userId " +
      "ORDER BY CASE WHEN r.lastMessageAt IS NULL THEN 1 ELSE 0 END, r.lastMessageAt DESC")
  Page<DmRoom> findMyRooms(@Param("userId") Long userId, Pageable pageable);
}
