package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.planwith.planwith_fo_comment.domain.comment.ModerationStatus;

public interface StoryCommentJpaRepository extends JpaRepository<StoryCommentJpaEntity, Long> {

	Optional<StoryCommentJpaEntity> findByCommentUuid(UUID commentUuid);

	@Query("""
			select c from StoryCommentJpaEntity c
			where c.storyUuid = :storyUuid
			  and c.moderationStatus = :moderationStatus
			  and (
			    c.deletedAt is null
			    or (
			      c.parentCommentUuid is null
			      and c.deletedAt is not null
			      and exists (
			        select r.commentId from StoryCommentJpaEntity r
			        where r.parentCommentUuid = c.commentUuid
			          and r.deletedAt is null
			          and r.moderationStatus = :moderationStatus
			      )
			    )
			  )
			order by c.createdAt asc
			""")
	List<StoryCommentJpaEntity> findVisibleListByStoryUuid(
			@Param("storyUuid") UUID storyUuid,
			@Param("moderationStatus") ModerationStatus moderationStatus
	);

	List<StoryCommentJpaEntity> findByStoryUuidAndDeletedAtIsNullAndModerationStatusOrderByCreatedAtAsc(
			UUID storyUuid,
			ModerationStatus moderationStatus
	);

	List<StoryCommentJpaEntity> findByStoryUuidAndModerationStatusAndDeletedAtIsNullOrderByReportCountDescCreatedAtDesc(
			UUID storyUuid,
			ModerationStatus moderationStatus
	);

	List<StoryCommentJpaEntity> findByStoryUuidAndParentCommentUuidAndDeletedAtIsNullOrderByCreatedAtAsc(
			UUID storyUuid,
			UUID parentCommentUuid
	);
}
