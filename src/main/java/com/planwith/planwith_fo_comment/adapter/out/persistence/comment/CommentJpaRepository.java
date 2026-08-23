package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_comment.domain.comment.CommentStatus;

public interface CommentJpaRepository extends JpaRepository<CommentJpaEntity, UUID> {

	List<CommentJpaEntity> findByStoryUuidAndStatusOrderByCreatedAtAsc(UUID storyUuid, CommentStatus status);
}
