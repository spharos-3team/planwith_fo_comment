package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import com.planwith.planwith_fo_comment.domain.comment.StoryComment;

final class StoryCommentPersistenceMapper {

	private StoryCommentPersistenceMapper() {
	}

	static void copyToEntity(StoryComment comment, StoryCommentJpaEntity entity) {
		entity.setCommentUuid(comment.getCommentUuid());
		entity.setStoryUuid(comment.getStoryUuid());
		entity.setMemberUuid(comment.getMemberUuid());
		entity.setParentCommentUuid(comment.getParentCommentUuid());
		entity.setCommentContent(comment.getCommentContent());
		entity.setCommentLikeCount(comment.getCommentLikeCount());
		entity.setReportCount(comment.getReportCount());
		entity.setModerationStatus(comment.getModerationStatus());
		entity.setHiddenAt(comment.getHiddenAt());
		entity.setCreatedAt(comment.getCreatedAt());
		entity.setUpdatedAt(comment.getUpdatedAt());
		entity.setDeletedAt(comment.getDeletedAt());
	}

	static StoryComment toDomain(StoryCommentJpaEntity entity) {
		return StoryComment.restore(
				entity.getCommentId(),
				entity.getCommentUuid(),
				entity.getStoryUuid(),
				entity.getMemberUuid(),
				entity.getParentCommentUuid(),
				entity.getCommentContent(),
				entity.getCommentLikeCount(),
				entity.getReportCount(),
				entity.getModerationStatus(),
				entity.getHiddenAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt(),
				entity.getDeletedAt()
		);
	}
}
