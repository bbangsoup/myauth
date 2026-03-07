package com.example.myauth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 게시글 조회 기록 엔티티
 * 같은 사용자가 같은 게시글을 중복 조회하는 것을 방지하기 위해 사용한다.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "post_views",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_post_view_viewer_post",
            columnNames = {"viewer_id", "post_id"}
        )
    },
    indexes = {
        @Index(name = "idx_post_view_post", columnList = "post_id"),
        @Index(name = "idx_post_view_viewer", columnList = "viewer_id")
    }
)
public class PostView {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "viewer_id", nullable = false)
  private User viewer;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  private Post post;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}
