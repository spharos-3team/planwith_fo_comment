package com.planwith.planwith_fo_comment.domain.comment;

import java.time.Instant;
import java.util.UUID;

import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidCommentContentException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidReplyException;

public class StoryComment {

	public static final int MAX_CONTENT_LENGTH = 1000;

	private Long commentId;
	private final UUID commentUuid;
	private final UUID storyUuid;
	private final UUID memberUuid;
	private final UUID parentCommentUuid;
	private String commentContent;
	private long commentLikeCount;
	private long reportCount;
	private ModerationStatus moderationStatus;
	private Instant hiddenAt;
	private final Instant createdAt;
	private Instant updatedAt;
	private Instant deletedAt;

	private StoryComment(
			Long commentId,
			UUID commentUuid,
			UUID storyUuid,
			UUID memberUuid,
			UUID parentCommentUuid,
			String commentContent,
			long commentLikeCount,
			long reportCount,
			ModerationStatus moderationStatus,
			Instant hiddenAt,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		this.commentId = commentId;
		this.commentUuid = commentUuid;
		this.storyUuid = storyUuid;
		this.memberUuid = memberUuid;
		this.parentCommentUuid = parentCommentUuid;
		this.commentContent = commentContent;
		this.commentLikeCount = commentLikeCount;
		this.reportCount = reportCount;
		this.moderationStatus = moderationStatus;
		this.hiddenAt = hiddenAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.deletedAt = deletedAt;
	}

	public static StoryComment createRoot(UUID storyUuid, UUID memberUuid, String commentContent) {
		return createNew(storyUuid, memberUuid, null, commentContent);
	}

	public static StoryComment createReply(StoryComment parent, UUID memberUuid, String commentContent) {
		parent.assertCanReceiveReply();
		return createNew(parent.getStoryUuid(), memberUuid, parent.getCommentUuid(), commentContent);
	}

	private static StoryComment createNew(
			UUID storyUuid,
			UUID memberUuid,
			UUID parentCommentUuid,
			String commentContent
	) {
		validateContent(commentContent);
		Instant now = Instant.now();
		return new StoryComment(
				null,
				UUID.randomUUID(),
				storyUuid,
				memberUuid,
				parentCommentUuid,
				commentContent,
				0L,
				0L,
				ModerationStatus.VISIBLE,
				null,
				now,
				now,
				null
		);
	}

	public static StoryComment restore(
			Long commentId,
			UUID commentUuid,
			UUID storyUuid,
			UUID memberUuid,
			UUID parentCommentUuid,
			String commentContent,
			long commentLikeCount,
			long reportCount,
			ModerationStatus moderationStatus,
			Instant hiddenAt,
			Instant createdAt,
			Instant updatedAt,
			Instant deletedAt
	) {
		return new StoryComment(
				commentId,
				commentUuid,
				storyUuid,
				memberUuid,
				parentCommentUuid,
				commentContent,
				commentLikeCount,
				reportCount,
				moderationStatus,
				hiddenAt,
				createdAt,
				updatedAt,
				deletedAt
		);
	}

	public void assignCommentId(Long commentId) {
		if (this.commentId == null) {
			this.commentId = commentId;
		}
	}

	public void updateContent(UUID requesterMemberUuid, String newContent) {
		assertNotDeleted();
		assertOwnedBy(requesterMemberUuid);
		validateContent(newContent);
		this.commentContent = newContent;
		this.updatedAt = Instant.now();
	}

	public void delete(UUID requesterMemberUuid) {
		assertNotDeleted();
		assertOwnedBy(requesterMemberUuid);
		Instant now = Instant.now();
		this.deletedAt = now;
		this.updatedAt = now;
	}

	public void hide() {
		assertNotDeleted();
		if (moderationStatus == ModerationStatus.HIDDEN) {
			return;
		}
		Instant now = Instant.now();
		this.moderationStatus = ModerationStatus.HIDDEN;
		this.hiddenAt = now;
		this.updatedAt = now;
	}

	public void increaseLikeCount() {
		assertNotDeleted();
		this.commentLikeCount += 1;
		this.updatedAt = Instant.now();
	}

	public void decreaseLikeCount() {
		assertNotDeleted();
		if (this.commentLikeCount > 0) {
			this.commentLikeCount -= 1;
			this.updatedAt = Instant.now();
		}
	}

	public void assertCanReceiveReply() {
		assertNotDeleted();
		if (!isRoot()) {
			throw new InvalidReplyException("대댓글에는 다시 대댓글을 작성할 수 없습니다.");
		}
		if (!isVisible()) {
			throw new InvalidReplyException("숨김 처리된 댓글에는 대댓글을 작성할 수 없습니다.");
		}
	}

	public boolean isRoot() {
		return parentCommentUuid == null;
	}

	public boolean isReply() {
		return parentCommentUuid != null;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}

	public boolean isVisible() {
		return !isDeleted() && moderationStatus == ModerationStatus.VISIBLE;
	}

	public boolean isActive() {
		return !isDeleted();
	}

	private void assertOwnedBy(UUID requesterMemberUuid) {
		if (!memberUuid.equals(requesterMemberUuid)) {
			throw new CommentOwnerMismatchException(commentUuid);
		}
	}

	private void assertNotDeleted() {
		if (isDeleted()) {
			throw new CommentAlreadyDeletedException(commentUuid);
		}
	}

	private static void validateContent(String commentContent) {
		if (commentContent == null || commentContent.isBlank()) {
			throw new InvalidCommentContentException("댓글 내용은 필수입니다.");
		}
		if (commentContent.length() > MAX_CONTENT_LENGTH) {
			throw new InvalidCommentContentException("댓글 내용은 1000자를 초과할 수 없습니다.");
		}
	}

	public Long getCommentId() {
		return commentId;
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

	public UUID getParentCommentUuid() {
		return parentCommentUuid;
	}

	public String getCommentContent() {
		return commentContent;
	}

	public long getCommentLikeCount() {
		return commentLikeCount;
	}

	public long getReportCount() {
		return reportCount;
	}

	public ModerationStatus getModerationStatus() {
		return moderationStatus;
	}

	public Instant getHiddenAt() {
		return hiddenAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public Instant getDeletedAt() {
		return deletedAt;
	}
}
