package com.example.myauth.dto.post;

import com.example.myauth.entity.Post;
import com.example.myauth.entity.Visibility;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 寃뚯떆湲 紐⑸줉 ?묐떟 DTO
 * 寃뚯떆湲 紐⑸줉 議고쉶 ??諛섑솚?섎뒗 媛꾨왂???뺣낫
 * (?곸꽭 議고쉶蹂대떎 ?곸? ?뺣낫 ?ы븿 - ?깅뒫 理쒖쟻??
 *
 * ?먯쓳???덉떆??
 * {
 *   "id": 1,
 *   "content": "?ㅻ뒛 留쏆엳?????..",
 *   "thumbnailUrl": "http://...",
 *   "imageCount": 3,
 *   "likeCount": 42,
 *   "commentCount": 5,
 *   "author": {
 *     "id": 1,
 *     "name": "?띻만??,
 *     "profileImage": "http://..."
 *   },
 *   "createdAt": "2025-01-24T10:30:00"
 * }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListResponse {

  /**
   * 寃뚯떆湲 ID
   */
  private Long id;

  /**
   * 寃뚯떆湲 蹂몃Ц (誘몃━蹂닿린??- 理쒕? 100??
   */
  private String content;

  /**
   * 怨듦컻 踰붿쐞
   */
  private Visibility visibility;

  /**
   * ????대?吏 URL (泥?踰덉㎏ ?대?吏???몃꽕??
   */
  private String thumbnailUrl;

  /**
   * 泥⑤? ?대?吏 媛쒖닔
   */
  private Integer imageCount;

  /**
   * 醫뗭븘????
   */
  private Integer likeCount;

  /**
   * ?볤? ??
   */
  private Integer commentCount;

  private Integer viewCount;

  /**
   * ?묒꽦???뺣낫
   */
  private PostAuthorResponse author;

  /**
   * ?묒꽦 ?쇱떆
   */
  private LocalDateTime createdAt;

  /**
   * Entity ??DTO 蹂??
   */
  public static PostListResponse from(Post post) {
    // 蹂몃Ц 誘몃━蹂닿린 (理쒕? 100??
    String contentPreview = post.getContent();
    if (contentPreview != null && contentPreview.length() > 100) {
      contentPreview = contentPreview.substring(0, 100) + "...";
    }

    // 泥?踰덉㎏ ?대?吏???몃꽕??URL
    String thumbnailUrl = null;
    if (!post.getImages().isEmpty()) {
      thumbnailUrl = post.getImages().get(0).getThumbnailUrl();
      // ?몃꽕?쇱씠 ?놁쑝硫??먮낯 ?대?吏 URL ?ъ슜
      if (thumbnailUrl == null) {
        thumbnailUrl = post.getImages().get(0).getImageUrl();
      }
    }

    return PostListResponse.builder()
        .id(post.getId())
        .content(contentPreview)
        .visibility(post.getVisibility())
        .thumbnailUrl(thumbnailUrl)
        .imageCount(post.getImages().size())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .viewCount(post.getViewCount())
        .author(PostAuthorResponse.from(post.getUser()))
        .createdAt(post.getCreatedAt())
        .build();
  }
}