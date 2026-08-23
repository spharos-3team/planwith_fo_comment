package com.planwith.planwith_fo_comment.domain.comment;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;

public class Comment {

	private final UUID commentUuid;
	private final UUID storyUuid;
	private final UUID memberUuid;
	private String content;
	private CommentStatus status;
	private long likeCount;
	private final Instant createdAt;
	private Instant updatedAt;

	private Comment(
			UUID commentUuid,
			UUID storyUuid,
			UUID memberUuid,
			String content,
			CommentStatus status,
			long likeCount,
			Instant createdAt,
			Instant updatedAt
	) {
		this.commentUuid = commentUuid;
		this.storyUuid = storyUuid;
		this.memberUuid = memberUuid;
		this.content = content;
		this.status = status;
		this.likeCount = likeCount;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	public static Comment create(UUID storyUuid, UUID memberUuid, String content) {
		Instant now = Instant.now();
		return new Comment(
				UUID.randomUUID(),
				storyUuid,
				memberUuid,
				content,
				CommentStatus.ACTIVE,
				0L,
				now,
				now
		);
	}

	public static Comment restore(
			UUID commentUuid,
			UUID storyUuid,
			UUID memberUuid,
			String content,
			CommentStatus status,
			long likeCount,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Comment(
				commentUuid,
				storyUuid,
				memberUuid,
				content,
				status,
				likeCount,
				createdAt,
				updatedAt
		);
	}

	public void updateContent(UUID requesterMemberUuid, String newContent) {
		assertActive();
		assertOwnedBy(requesterMemberUuid);
		this.content = newContent;
		this.updatedAt = Instant.now();
	}

	public void delete(UUID requesterMemberUuid) {
		assertActive();
		assertOwnedBy(requesterMemberUuid);
		this.status = CommentStatus.DELETED;
		this.updatedAt = Instant.now();
	}

	public void increaseLikeCount() {
		assertActive();
		this.likeCount += 1;
		this.updatedAt = Instant.now();
	}

	public void decreaseLikeCount() {
		assertActive();
		if (this.likeCount > 0) {
			this.likeCount -= 1;
			this.updatedAt = Instant.now();
		}
	}

	public boolean isActive() {
		return status == CommentStatus.ACTIVE;
	}

	private void assertOwnedBy(UUID requesterMemberUuid) {
		if (!memberUuid.equals(requesterMemberUuid)) {
			throw new CommentOwnerMismatchException(commentUuid);
		}
	}

	private void assertActive() {
		if (!isActive()) {
			throw new CommentAlreadyDeletedException(commentUuid);
		}
	}

	public UUID getCommentUuid() {
		return commentUuid;
	}

	public UUID getStoryUuid() {
		return storyUuid;
	}

	public UUID getMemberUuid() {
		return memberUuid;
	}

	public String getContent() {
		return content;
	}

	public CommentStatus getStatus() {
		return status;
	}

	public long getLikeCount() {
		return likeCount;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}
}
