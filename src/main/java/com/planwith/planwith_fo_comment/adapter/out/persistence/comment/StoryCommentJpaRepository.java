package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.planwith.planwith_fo_comment.domain.comment.ModerationStatus;

public interface StoryCommentJpaRepository extends JpaRepository<StoryCommentJpaEntity, Long> {

	Optional<StoryCommentJpaEntity> findByCommentUuid(UUID commentUuid);

	List<StoryCommentJpaEntity> findByStoryUuidAndDeletedAtIsNullAndModerationStatusOrderByCreatedAtAsc(
			UUID storyUuid,
			ModerationStatus moderationStatus
	);

	List<StoryCommentJpaEntity> findByStoryUuidAndParentCommentUuidAndDeletedAtIsNullOrderByCreatedAtAsc(
			UUID storyUuid,
			UUID parentCommentUuid
	);
}
