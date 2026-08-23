package com.planwith.planwith_fo_comment.domain.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.planwith.planwith_fo_comment.domain.exception.CommentAlreadyDeletedException;
import com.planwith.planwith_fo_comment.domain.exception.CommentOwnerMismatchException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidCommentContentException;
import com.planwith.planwith_fo_comment.domain.exception.InvalidReplyException;

class StoryCommentTest {

	@Test
	void createRootCommentWithDefaultVisibleState() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();

		StoryComment comment = StoryComment.createRoot(storyUuid, memberUuid, "첫 댓글");

		assertThat(comment.getStoryUuid()).isEqualTo(storyUuid);
		assertThat(comment.getMemberUuid()).isEqualTo(memberUuid);
		assertThat(comment.getParentCommentUuid()).isNull();
		assertThat(comment.isRoot()).isTrue();
		assertThat(comment.getModerationStatus()).isEqualTo(ModerationStatus.VISIBLE);
		assertThat(comment.getCommentLikeCount()).isZero();
		assertThat(comment.getReportCount()).isZero();
		assertThat(comment.getDeletedAt()).isNull();
		assertThat(comment.getCommentUuid()).isNotNull();
	}

	@Test
	void createOneLevelReplyUnderRootComment() {
		StoryComment parent = StoryComment.createRoot(UUID.randomUUID(), UUID.randomUUID(), "부모 댓글");

		StoryComment reply = StoryComment.createReply(parent, UUID.randomUUID(), "대댓글");

		assertThat(reply.isReply()).isTrue();
		assertThat(reply.getParentCommentUuid()).isEqualTo(parent.getCommentUuid());
		assertThat(reply.getStoryUuid()).isEqualTo(parent.getStoryUuid());
	}

	@Test
	void rejectNestedReplyBeyondOneLevel() {
		StoryComment parent = StoryComment.createRoot(UUID.randomUUID(), UUID.randomUUID(), "부모 댓글");
		StoryComment reply = StoryComment.createReply(parent, UUID.randomUUID(), "대댓글");

		assertThatThrownBy(() -> StoryComment.createReply(reply, UUID.randomUUID(), "중첩 대댓글"))
				.isInstanceOf(InvalidReplyException.class)
				.hasMessage("대댓글에는 다시 대댓글을 작성할 수 없습니다.");
	}

	@Test
	void rejectReplyToDeletedOrHiddenParent() {
		UUID ownerUuid = UUID.randomUUID();
		StoryComment deletedParent = StoryComment.createRoot(UUID.randomUUID(), ownerUuid, "삭제 예정");
		deletedParent.delete(ownerUuid);

		assertThatThrownBy(() -> StoryComment.createReply(deletedParent, UUID.randomUUID(), "대댓글"))
				.isInstanceOf(CommentAlreadyDeletedException.class);

		StoryComment hiddenParent = StoryComment.createRoot(UUID.randomUUID(), ownerUuid, "숨김 예정");
		hiddenParent.hide();

		assertThatThrownBy(() -> StoryComment.createReply(hiddenParent, UUID.randomUUID(), "대댓글"))
				.isInstanceOf(InvalidReplyException.class)
				.hasMessage("숨김 처리된 댓글에는 대댓글을 작성할 수 없습니다.");
	}

	@Test
	void updateAndDeleteAreAllowedOnlyForOwner() {
		UUID ownerUuid = UUID.randomUUID();
		StoryComment comment = StoryComment.createRoot(UUID.randomUUID(), ownerUuid, "원본");

		comment.updateContent(ownerUuid, "수정");
		assertThat(comment.getCommentContent()).isEqualTo("수정");

		assertThatThrownBy(() -> comment.delete(UUID.randomUUID()))
				.isInstanceOf(CommentOwnerMismatchException.class);

		comment.delete(ownerUuid);
		assertThat(comment.isDeleted()).isTrue();
		assertThat(comment.getDeletedAt()).isNotNull();
		assertThatThrownBy(() -> comment.updateContent(ownerUuid, "다시"))
				.isInstanceOf(CommentAlreadyDeletedException.class);
	}

	@Test
	void likeCountNeverGoesBelowZero() {
		StoryComment comment = StoryComment.createRoot(UUID.randomUUID(), UUID.randomUUID(), "좋아요");

		comment.increaseLikeCount();
		comment.increaseLikeCount();
		comment.decreaseLikeCount();
		comment.decreaseLikeCount();
		comment.decreaseLikeCount();

		assertThat(comment.getCommentLikeCount()).isZero();
	}

	@Test
	void rejectBlankOrTooLongContent() {
		UUID storyUuid = UUID.randomUUID();
		UUID memberUuid = UUID.randomUUID();

		assertThatThrownBy(() -> StoryComment.createRoot(storyUuid, memberUuid, " "))
				.isInstanceOf(InvalidCommentContentException.class);

		assertThatThrownBy(() -> StoryComment.createRoot(storyUuid, memberUuid, "a".repeat(1001)))
				.isInstanceOf(InvalidCommentContentException.class);
	}
}
