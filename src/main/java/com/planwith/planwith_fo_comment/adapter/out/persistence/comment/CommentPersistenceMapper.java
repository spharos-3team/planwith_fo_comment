package com.planwith.planwith_fo_comment.adapter.out.persistence.comment;

import com.planwith.planwith_fo_comment.domain.comment.Comment;

final class CommentPersistenceMapper {

	private CommentPersistenceMapper() {
	}

	static CommentJpaEntity toEntity(Comment comment) {
		return CommentJpaEntity.builder()
				.commentUuid(comment.getCommentUuid())
				.storyUuid(comment.getStoryUuid())
				.memberUuid(comment.getMemberUuid())
				.content(comment.getContent())
				.status(comment.getStatus())
				.likeCount(comment.getLikeCount())
				.createdAt(comment.getCreatedAt())
				.updatedAt(comment.getUpdatedAt())
				.build();
	}

	static Comment toDomain(CommentJpaEntity entity) {
		return Comment.restore(
				entity.getCommentUuid(),
				entity.getStoryUuid(),
				entity.getMemberUuid(),
				entity.getContent(),
				entity.getStatus(),
				entity.getLikeCount(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
