package com.example.myauth.repository;

import com.example.myauth.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostViewRepository extends JpaRepository<PostView, Long> {

  boolean existsByViewerIdAndPostId(Long viewerId, Long postId);
}
